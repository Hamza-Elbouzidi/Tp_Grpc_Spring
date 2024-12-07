package com.grpc.tp_grpc_spring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Table(name = "comptes")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Compte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private float solde;
    private String dateCreation;
    @Enumerated(EnumType.STRING)
    private TypeCompte type;
}
