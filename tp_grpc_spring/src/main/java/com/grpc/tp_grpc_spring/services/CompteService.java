package com.grpc.tp_grpc_spring.services;

import com.grpc.tp_grpc_spring.entity.Compte;
import com.grpc.tp_grpc_spring.repository.CompteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompteService {
    private final CompteRepository compteRepository;

    public CompteService(CompteRepository compteRepository) {
        this.compteRepository = compteRepository;
    }

    public List<Compte> findAllComptes() {
        return compteRepository.findAll();
    }

    public Compte findCompteById(Long id) {
        return compteRepository.findById(id).orElse(null);
    }

    public Compte saveCompte(Compte compte) {
        return compteRepository.save(compte);
    }
    public void deleteCompte(Long id) {
        compteRepository.deleteById(id);
    }
    public List<Compte> findComptesByType(String type) {
        return compteRepository.findByType(type);
    }

}
