package eu.einfracentral.registry.controller;

import eu.einfracentral.domain.Measurement;
import eu.einfracentral.registry.service.MeasurementService;
import eu.openminted.registry.core.exception.ResourceNotFoundException;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("measurement")
public class MeasurementController extends ResourceController<Measurement, Authentication> {
    @Autowired
    MeasurementController(MeasurementService service) {
        super(service);
    }

    @ApiOperation(value = "Returns the measurement assigned the given id.")
    @RequestMapping(path = "{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<Measurement> get(@PathVariable("id") String id, Authentication authentication) throws ResourceNotFoundException {
        return super.get(id, authentication);
    }

    @CrossOrigin
    @ApiOperation(value = "Adds the given measurement.")
    @RequestMapping(method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<Measurement> add(@RequestBody Measurement measurement, Authentication authentication) throws Exception {
        return super.add(measurement, authentication);
    }

    @ApiOperation(value = "Updates the measurement assigned the given id with the given measurement, keeping a versions of revisions.")
    @RequestMapping(method = RequestMethod.PUT, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<Measurement> update(@RequestBody Measurement measurement, Authentication jwt) throws Exception {
        return super.update(measurement, jwt);
    }
}
