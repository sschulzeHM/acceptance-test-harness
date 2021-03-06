package org.jenkinsci.test.acceptance.update_center;

import com.google.inject.Injector;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM;

/**
 * Databinding for installable plugin in UC.
 *
 * @author Kohsuke Kawaguchi
 */
public class PluginMetadata {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginMetadata.class);
    public String name, version, gav;
    public List<Dependency> dependencies;
    public URL url;

    void init(UpdateCenterMetadata parent) {
        for (Dependency d : dependencies) {
            d.init(parent);
        }
    }

    /**
     * @param jenkins
     * @param i
     * @param version The version of the plugin you want to upload
     * @throws ArtifactResolutionException
     * @throws IOException
     */
    public void uploadTo(Jenkins jenkins, Injector i, String version) throws ArtifactResolutionException, IOException {
        RepositorySystem rs = i.getInstance(RepositorySystem.class);
        RepositorySystemSession rss = i.getInstance(RepositorySystemSession.class);

        ArtifactResult r = rs.resolveArtifact(rss, new ArtifactRequest(
                makeArtifact(version == null ? this.version : version),
                Arrays.asList(new RemoteRepository.Builder("repo.jenkins-ci.org", "default", "http://repo.jenkins-ci.org/public/").build()),
                null));
        LOGGER.info("Installing plugin: [{}]",r.getArtifact());
        HttpClient httpclient = new DefaultHttpClient();

        HttpPost post = new HttpPost(jenkins.url("pluginManager/uploadPlugin").toExternalForm());
        HttpEntity e = MultipartEntityBuilder.create()
                .addBinaryBody("name", r.getArtifact().getFile(), APPLICATION_OCTET_STREAM, name + ".jpi")
                .build();
        post.setEntity(e);

        HttpResponse response = httpclient.execute(post);
        if (response.getStatusLine().getStatusCode() >= 400)
            throw new IOException("Failed to upload plugin: " + response.getStatusLine() + "\n" +
                    IOUtils.toString(response.getEntity().getContent()));
    }

    private DefaultArtifact makeArtifact(String version) {
        String[] t = gav.split(":");
        return new DefaultArtifact(t[0], t[1], "hpi", version);
    }

    @Override
    public String toString() {
        return super.toString() + "[" + name + "," + version + "]";
    }


}
