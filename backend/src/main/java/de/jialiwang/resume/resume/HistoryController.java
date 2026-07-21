package de.jialiwang.resume.resume;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/history")
public class HistoryController {
    private final HistoryService service;

    public HistoryController(HistoryService service) { this.service = service; }

    @GetMapping
    List<HistoryService.Entry> list() { return service.list(); }

    @DeleteMapping("/{applicationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID applicationId) { service.delete(applicationId); }
}
