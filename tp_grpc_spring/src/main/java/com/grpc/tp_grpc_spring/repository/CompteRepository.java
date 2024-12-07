package com.grpc.tp_grpc_spring.repository;


import com.grpc.tp_grpc_spring.entity.Compte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface CompteRepository extends JpaRepository<Compte, Long> {

    List<Compte> findByType(String type);

}