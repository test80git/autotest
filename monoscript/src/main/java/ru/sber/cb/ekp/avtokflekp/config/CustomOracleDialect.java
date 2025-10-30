//package ru.sber.cb.ekp.avtokflekp.config;
//
//import org.hibernate.boot.model.FunctionContributions;
//import org.hibernate.dialect.OracleDialect;
//import org.hibernate.query.sqm.function.FunctionKind;
//import org.hibernate.query.sqm.function.SqmFunctionRegistry;
//import org.hibernate.query.sqm.produce.function.PatternFunctionDescriptorBuilder;
//import org.hibernate.type.spi.TypeConfiguration;
//
//public class CustomOracleDialect extends OracleDialect {
//    @Override
//    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
//        super.initializeFunctionRegistry(functionContributions);
//        SqmFunctionRegistry registry = functionContributions.getFunctionRegistry();
//        TypeConfiguration types = functionContributions.getTypeConfiguration();
//
//
//        new PatternFunctionDescriptorBuilder(registry, "random", FunctionKind.NORMAL, "DBMS_RANDOM.VALUE(1, 1000)")
//                .setExactArgumentCount(0)
//                .register();
//    }
//}