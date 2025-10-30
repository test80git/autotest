package ru.sber.cb.ekp.avtokflekp.subsystem.loans.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import ru.sber.cb.ekp.annotation.Description;
import ru.sber.cb.ekp.annotation.Step;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Step
@Entity
@Data
@Table(name = "PR_CRED")
@Description("Кредиты")
public class PrCred {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pr_cred_seq")
    @SequenceGenerator(name = "pr_cred_seq", sequenceName = "pr_cred_seq", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "C_SUMMA_DOG", precision = 15, scale = 2)
    @Description("Сумма договора")
    private BigDecimal summaDog;

    @Column(name = "C_PARAM_FOR_PLAN")
    @Description("Параметры планирования операций")
    private Long cParamForPlan;

    @Column(name = "C_COMISS_ARR")
    @Description("Комиссии по задолженностям")
    private Long cComissArr;

    @Column(name = "C_DATE_GIVE")
    @Description("Дата выдачи")
    private Timestamp dateGive;

}
