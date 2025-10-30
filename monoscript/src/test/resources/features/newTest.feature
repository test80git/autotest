@newTest

Feature:

  Scenario:

    Given Поиск PrCred по условию All
    And Записываю 1234567890L в PrCred.cComissArr
    And Проверяю что PrCred.cComissArr == 1234567890