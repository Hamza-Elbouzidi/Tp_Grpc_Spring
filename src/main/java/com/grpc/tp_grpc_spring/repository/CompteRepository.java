package com.grpc.tp_grpc_spring.repository;


import com.grpc.tp_grpc_spring.entity.Compte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompteRepository extends JpaRepository<Compte, String> {

    List<Compte> findByType(String type);

}