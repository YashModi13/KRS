package com.krs.backend.controllers;
import com.krs.backend.models.Tender;
import com.krs.backend.repositories.TenderRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "${app.cors.origins}", maxAge = 3600)
@RestController
@RequestMapping("/api/tenders")
public class TenderController {
    private final TenderRepository tenderRepository;

    public TenderController(TenderRepository tenderRepository) {
        this.tenderRepository = tenderRepository;
    }
    
    @GetMapping
    public List<Tender> getAllTenders() { return tenderRepository.findAll(); }
    
    @PostMapping
    public Tender createTender(@RequestBody Tender tender) { return tenderRepository.save(tender); }
}
