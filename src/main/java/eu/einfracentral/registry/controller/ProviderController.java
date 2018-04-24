package eu.einfracentral.registry.controller;

import eu.einfracentral.domain.Provider;
import eu.einfracentral.registry.service.ProviderService;
import eu.openminted.registry.core.exception.ResourceNotFoundException;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by pgl on 26/07/17.
 */
@RestController
@RequestMapping("provider")
public class ProviderController extends ResourceController<Provider> {
    @Autowired
    ProviderController(ProviderService service) {
        super(service);
    }

    @ApiOperation(value = "Updates provider info")
    @RequestMapping(method = RequestMethod.PUT, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<Provider> update(@RequestBody Provider provider, @ApiIgnore @CookieValue(defaultValue = "") String jwt) throws ResourceNotFoundException {
        return super.update(provider, jwt);
    }
}
