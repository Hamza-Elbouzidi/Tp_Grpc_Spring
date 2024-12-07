package com.grpc.tp_grpc_spring.controllers;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import com.grpc.tp_grpc_spring.stubs.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@GrpcService
public class CompteServiceImpl extends CompteServiceGrpc.CompteServiceImplBase {

    private final Map<String, Compte> compteDB = new ConcurrentHashMap<>();

    /**
     * Get all comptes.
     */
    @Override
    public void allComptes(GetAllComptesRequest request, StreamObserver<GetAllComptesResponse> responseObserver) {
        try {
            GetAllComptesResponse.Builder responseBuilder = GetAllComptesResponse.newBuilder();
            responseBuilder.addAllComptes(compteDB.values());
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    /**
     * Get compte by ID.
     */
    @Override
    public void compteById(GetCompteByIdRequest request, StreamObserver<GetCompteByIdResponse> responseObserver) {
        try {
            Compte compte = compteDB.get(request.getId());
            if (compte != null) {
                responseObserver.onNext(GetCompteByIdResponse.newBuilder().setCompte(compte).build());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Throwable("Compte non trouvé"));
            }
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    /**
     * Get total solde.
     */
    @Override
    public void getTotalSolde(GetTotalSoldeRequest request, StreamObserver<GetTotalSoldeResponse> responseObserver) {
        try {
            int count = compteDB.size();
            float sum = 0;
            for (Compte compte : compteDB.values()) {
                sum += compte.getSolde();
            }
            float average = count > 0 ? sum / count : 0;

            SoldeStats stats = SoldeStats.newBuilder()
                    .setCount(count)
                    .setSum(sum)
                    .setAverage(average)
                    .build();

            responseObserver.onNext(GetTotalSoldeResponse.newBuilder().setStats(stats).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    /**
     * Save a new compte.
     */
    @Override
    public void saveCompte(SaveCompteRequest request, StreamObserver<SaveCompteResponse> responseObserver) {
        try {
            CompteRequest compteReq = request.getCompte();
            String id = UUID.randomUUID().toString();

            Compte compte = Compte.newBuilder()
                    .setId(id)
                    .setSolde(compteReq.getSolde())
                    .setDateCreation(compteReq.getDateCreation())
                    .setType(compteReq.getType())
                    .build();

            compteDB.put(id, compte);

            responseObserver.onNext(SaveCompteResponse.newBuilder().setCompte(compte).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    /**
     * Delete a compte by ID.
     */
    @Override
    public void deleteCompte(DeleteCompteRequest request, StreamObserver<DeleteCompteResponse> responseObserver) {
        try {
            String compteId = request.getId();
            if (compteDB.containsKey(compteId)) {
                compteDB.remove(compteId);
                String successMessage = "Compte with ID " + compteId + " deleted successfully.";
                responseObserver.onNext(DeleteCompteResponse.newBuilder().setMessage(successMessage).build());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Throwable("Compte with ID " + compteId + " not found."));
            }
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    /**
     * Get all comptes by type.
     */
    @Override
    public void getComptesByType(GetComptesByTypeRequest request, StreamObserver<GetComptesByTypeResponse> responseObserver) {
        try {
            List<Compte> comptesByType = new ArrayList<>();
            for (Compte compte : compteDB.values()) {
                if (compte.getType() == request.getType()) {
                    comptesByType.add(compte);
                }
            }
            GetComptesByTypeResponse response = GetComptesByTypeResponse.newBuilder().addAllComptes(comptesByType).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
