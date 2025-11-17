@Test2
@link=https://jira.t.ru/secure/Tests.jspa#/testCase/Test2
Feature:

  Scenario: Поиск кредита с суммой договора меньше 1 млн

    Given Поиск PrCred по условию SumDogLessOrderByDesc
      | sumdog | 1000000.00 |

    And Проверяю что PrCred.id == 4