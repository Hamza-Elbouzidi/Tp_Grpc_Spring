package com.grpc.tp_grpc_spring.controllers;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import com.grpc.tp_grpc_spring.stubs.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@GrpcService
public class CompteServiceImpl extends CompteServiceGrpc.CompteServiceImplBase {

    // Use Integer as the key for ID and Compte as the value
    private final Map<Integer, Compte> compteDB = new ConcurrentHashMap<>();

    // Auto-increment ID generator
    private final AtomicInteger idGenerator = new AtomicInteger(0);

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
            // Step 1: Extract the ID from the request
            int compteId = request.getId(); // Use int, no need for Long

            // Step 2: Look up the Compte by ID in the `compteDB`
            Compte compte = compteDB.get(compteId); // Integer-based lookup

            if (compte != null) {
                // Step 3: Send response if the Compte exists
                responseObserver.onNext(GetCompteByIdResponse.newBuilder().setCompte(compte).build());
                responseObserver.onCompleted();
            } else {
                // Step 4: Send "not found" response
                responseObserver.onError(
                        Status.NOT_FOUND.withDescription("Compte with ID " + compteId + " not found.").asRuntimeException()
                );
            }
        } catch (Exception e) {
            // Step 5: Send internal server error
            responseObserver.onError(
                    Status.INTERNAL.withDescription("An internal error occurred: " + e.getMessage()).asRuntimeException()
            );
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

            int newId = idGenerator.incrementAndGet(); // Generate unique ID

            Compte compte = Compte.newBuilder()
                    .setId(newId) // Assign the auto-generated ID here
                    .setSolde(compteReq.getSolde())
                    .setDateCreation(compteReq.getDateCreation())
                    .setType(compteReq.getType())
                    .build();

            // Store the new compte in the database (in-memory map)
            compteDB.put(newId, compte);

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
            int compteId = request.getId(); // Get id as int
            if (compteDB.containsKey(compteId)) {
                compteDB.remove(compteId);
                String successMessage = "Compte with ID " + compteId + " deleted successfully.";
                responseObserver.onNext(DeleteCompteResponse.newBuilder().setMessage(successMessage).build());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(
                        Status.NOT_FOUND.withDescription("Compte with ID " + compteId + " not found.").asRuntimeException()
                );
            }
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("An internal error occurred: " + e.getMessage()).asRuntimeException()
            );
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
            responseObserver.onError(
                    Status.INTERNAL.withDescription("An internal error occurred: " + e.getMessage()).asRuntimeException()
            );
        }
    }
}
