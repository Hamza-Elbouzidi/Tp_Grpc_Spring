package com.grpc.tp_grpc_spring.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Compte {

    @Id
    private String id = UUID.randomUUID().toString();
    private float solde;
    private String dateCreation;
    private String type;
}