@Test1
@prcred
@link=https://jira.t.ru/secure/Tests.jspa#/testCase/Test1
Feature:

  Scenario: Поиск кредита с суммой договора больше 1 млн

    Given Поиск PrCred по условию SumDogMoreOrderByDesc
      | sumdog | 1000000.00 |

    And Проверяю что PrCred.id == 3