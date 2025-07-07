package com.example.jndi.web.controllers;

import com.example.jndi.exception.DriverNotFoundException;
import com.example.jndi.model.query.FindDriversQuery;
import com.example.jndi.model.request.DriverRequest;
import com.example.jndi.model.response.DriverResponse;
import com.example.jndi.model.response.PagedResult;
import com.example.jndi.services.DriverService;
import com.example.jndi.utils.AppConstants;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping
    public PagedResult<DriverResponse> getAllDrivers(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        FindDriversQuery findDriversQuery = new FindDriversQuery(pageNo, pageSize, sortBy, sortDir);
        return driverService.findAllDrivers(findDriversQuery);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable Long id) {
        return driverService
                .findDriverById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new DriverNotFoundException(id));
    }

    @PostMapping
    public ResponseEntity<DriverResponse> createDriver(@RequestBody @Validated DriverRequest driverRequest) {
        DriverResponse response = driverService.saveDriver(driverRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/api/drivers/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DriverResponse> updateDriver(
            @PathVariable Long id, @RequestBody @Valid DriverRequest driverRequest) {
        return ResponseEntity.ok(driverService.updateDriver(id, driverRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DriverResponse> deleteDriver(@PathVariable Long id) {
        return driverService
                .findDriverById(id)
                .map(driver -> {
                    driverService.deleteDriverById(id);
                    return ResponseEntity.ok(driver);
                })
                .orElseThrow(() -> new DriverNotFoundException(id));
    }
}
