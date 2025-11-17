@Test3
@prcred
@link=https://jira.t.ru/secure/Tests.jspa#/testCase/Test3
Feature: Test3

  Scenario: Поиск кредита

    Given Поиск PrCred по условию Id
      | id | 2 |

    And Проверяю что PrCred.id == 2