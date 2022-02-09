package com.github.alessandrobagnoli.verificac19.dao;

import javax.enterprise.context.ApplicationScoped;

import com.github.alessandrobagnoli.verificac19.model.RevokedPass;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

@ApplicationScoped
public class RevokedPassDAO implements PanacheRepositoryBase<RevokedPass, String> {
}
