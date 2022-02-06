package com.bagnoli.verificac19.dao;

import javax.enterprise.context.ApplicationScoped;

import com.bagnoli.verificac19.model.BlackListedPass;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

@ApplicationScoped
public class BlackListedPassDAO implements PanacheRepositoryBase<BlackListedPass, String> {
}
