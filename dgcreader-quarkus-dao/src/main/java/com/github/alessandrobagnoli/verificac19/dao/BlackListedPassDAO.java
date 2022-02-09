package com.github.alessandrobagnoli.verificac19.dao;

import javax.enterprise.context.ApplicationScoped;

import com.github.alessandrobagnoli.verificac19.model.BlackListedPass;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

@ApplicationScoped
public class BlackListedPassDAO implements PanacheRepositoryBase<BlackListedPass, String> {
}
