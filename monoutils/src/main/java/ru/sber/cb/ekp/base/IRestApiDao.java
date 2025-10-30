package ru.sber.cb.ekp.base;

public interface IRestApiDao<Rq, Rs, RqGen extends IRqGenerator<Rq>> {
    void post();
}