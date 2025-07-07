package com.example.jndi.services;

import com.example.jndi.entities.Driver;
import com.example.jndi.exception.DriverNotFoundException;
import com.example.jndi.mapper.DriverMapper;
import com.example.jndi.model.query.FindDriversQuery;
import com.example.jndi.model.request.DriverRequest;
import com.example.jndi.model.response.DriverResponse;
import com.example.jndi.model.response.PagedResult;
import com.example.jndi.repositories.DriverRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DriverService {

    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;

    public DriverService(DriverRepository driverRepository, DriverMapper driverMapper) {
        this.driverRepository = driverRepository;
        this.driverMapper = driverMapper;
    }

    public PagedResult<DriverResponse> findAllDrivers(FindDriversQuery findDriversQuery) {

        // create Pageable instance
        Pageable pageable = createPageable(findDriversQuery);

        Page<Driver> driversPage = driverRepository.findAll(pageable);

        List<DriverResponse> driverResponseList = driverMapper.toResponseList(driversPage.getContent());

        return new PagedResult<>(driversPage, driverResponseList);
    }

    private Pageable createPageable(FindDriversQuery findDriversQuery) {
        int pageNo = Math.max(findDriversQuery.pageNo() - 1, 0);
        Sort sort = Sort.by(
                findDriversQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.Order.asc(findDriversQuery.sortBy())
                        : Sort.Order.desc(findDriversQuery.sortBy()));
        return PageRequest.of(pageNo, findDriversQuery.pageSize(), sort);
    }

    public Optional<DriverResponse> findDriverById(Long id) {
        return driverRepository.findById(id).map(driverMapper::toResponse);
    }

    @Transactional
    public DriverResponse saveDriver(DriverRequest driverRequest) {
        Driver driver = driverMapper.toEntity(driverRequest);
        Driver savedDriver = driverRepository.save(driver);
        return driverMapper.toResponse(savedDriver);
    }

    @Transactional
    public DriverResponse updateDriver(Long id, DriverRequest driverRequest) {
        Driver driver = driverRepository.findById(id).orElseThrow(() -> new DriverNotFoundException(id));

        // Update the driver object with data from driverRequest
        driverMapper.mapDriverWithRequest(driver, driverRequest);

        // Save the updated driver object
        Driver updatedDriver = driverRepository.save(driver);

        return driverMapper.toResponse(updatedDriver);
    }

    @Transactional
    public void deleteDriverById(Long id) {
        driverRepository.deleteById(id);
    }
}
