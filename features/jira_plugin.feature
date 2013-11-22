Feature: Update JIRA tickets when a build is ready
  In order to notify people waiting for a bug fix
  As a Jenkins developer
  I want JIRA issues to be updated when a new build is made

  @docker
  Scenario: JIRA ticket gets updated with a build link
    Given a docker fixture "jira"
    And "ABC" project on docker jira fixture
    And a new issue in "ABC" project on docker jira fixture
    And a new issue in "ABC" project on docker jira fixture
    And I have installed the "jira" plugin

    Given I have installed the "git" plugin
    And an empty test git repository
    And a job

    Then I configure docker fixture as JIRA site

    Then I configure the job
    And I check out code from the test Git repository
    And I save the job

    Then I commit "initial commit" to the test Git repository
    And I build the job
    And the build should succeed

    When I commit "[ABC-1] fixed" to the test Git repository
    And I commit "[ABC-2] fixed" to the test Git repository
    And I build the job
    Then the build should succeed

    Then I debug

    Then I will write the rest of the test


  @nojenkins
  Scenario: quick test of local git repository
    Given an empty test git repository
    Then I commit "[ABC-1] fixed" to the test Git repository
    And I commit "[ABC-2] fixed" to the test Git repository

