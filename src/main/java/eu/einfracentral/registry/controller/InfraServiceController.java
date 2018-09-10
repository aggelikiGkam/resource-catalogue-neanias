package eu.einfracentral.registry.controller;

import eu.einfracentral.domain.InfraService;
import eu.einfracentral.domain.Service;
import eu.einfracentral.registry.service.InfraServiceService;
import eu.openminted.registry.core.domain.FacetFilter;
import eu.openminted.registry.core.domain.Paging;
import eu.openminted.registry.core.exception.ResourceNotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;


@RestController
@RequestMapping("infraService")
@ApiIgnore
@Api(value = "Get Information about a Service")
public class InfraServiceController {

    final static private Logger logger = LogManager.getLogger(InfraServiceController.class.getName());
  
    private InfraServiceService infraService;

    @Autowired
    InfraServiceController(InfraServiceService<InfraService, InfraService> service) {
        this.infraService = service;
    }

    @ApiIgnore
    @RequestMapping(path = {"delete/{id}/", "delete/{id}/{version}/"}, method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<InfraService> delete(@PathVariable("id") String id, @PathVariable Optional<String> version) throws ResourceNotFoundException {
        InfraService service;
        if (version.isPresent())
            service = infraService.get(id, version.get());
        else
            service = infraService.getLatest(id);
        infraService.delete(service);
//        Service ret = new Service(infraService.getLatest(id));
        return new ResponseEntity<>(HttpStatus.OK);
        //return super.get(id, jwt);
    }

    @ApiOperation(value = "Get the most current version of a specific infraService providing the infraService ID")
    @RequestMapping(path = "{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<InfraService> get(@PathVariable("id") String id, Authentication jwt) throws ResourceNotFoundException {
        return new ResponseEntity<>((InfraService) infraService.getLatest(id), HttpStatus.OK);
    }

    @ApiOperation(value = "Get the most current version of a specific infraService providing the infraService ID")
    @RequestMapping(path = "{id}/{version}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<InfraService> get(@PathVariable("id") String id, @PathVariable("version") String version,
                                            Authentication jwt) {
        InfraService ret = (InfraService) infraService.get(id, version);
        return new ResponseEntity<>(ret, ret != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @CrossOrigin
    @ApiOperation(value = "Adds the given infraService.")
    @RequestMapping(method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<InfraService> add(@RequestBody Service service, Authentication authentication) {
        return new ResponseEntity<>((InfraService) infraService.add(new InfraService(service), authentication), HttpStatus.OK);
    }

    @ApiOperation(value = "Updates the infraService assigned the given id with the given infraService, keeping a history of revisions.")
    @RequestMapping(method = RequestMethod.PUT, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<InfraService> update(@RequestBody Service service, @ApiIgnore Authentication authentication) throws ResourceNotFoundException {
        return new ResponseEntity<>((InfraService) infraService.update(new InfraService(service), authentication), HttpStatus.OK);
    }

    // TODO needs fixing
//    @ApiOperation(value = "Validates the infraService without actually changing the respository")
//    @RequestMapping(path = "validate", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
//    public ResponseEntity<InfraService> validate(@RequestBody Service service, Authentication jwt) {
//        return ResponseEntity.ok(infraService.validate(new InfraService(service)));
//    }

    @ApiOperation(value = "Filter a list of services based on a set of filters or get a list of all services in the eInfraCentral Catalogue  ")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "query", value = "Keyword to refine the search", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "from", value = "Starting index in the resultset", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "quantity", value = "Quantity of services to be fetched", dataType = "string", paramType = "query")
    })
    @RequestMapping(path = "all", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<Paging<InfraService>> getAll(@ApiIgnore @RequestParam Map<String, Object> allRequestParams, Authentication authentication) {
        FacetFilter ff = new FacetFilter();
        ff.setKeyword(allRequestParams.get("query") != null ? (String) allRequestParams.remove("query") : "");
        ff.setFrom(allRequestParams.get("from") != null ? Integer.parseInt((String) allRequestParams.remove("from")) : 0);
        ff.setQuantity(allRequestParams.get("quantity") != null ? Integer.parseInt((String) allRequestParams.remove("quantity")) : 10);
        ff.setFilter(allRequestParams);
        return ResponseEntity.ok(infraService.getAll(ff, authentication));
    }

    @ApiOperation(value = "Get a list of services based on a set of IDs")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "Comma-separated list of infraService ids", dataType = "string", paramType = "path")
    })
    @RequestMapping(path = "byID/{ids}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<List<InfraService>> getSome(@PathVariable String[] ids, Authentication jwt) {
        return ResponseEntity.ok(infraService.getByIds(ids));
    }

    @ApiOperation(value = "Get all services in the catalogue organized by an attribute, e.g. get infraService organized in categories ")
    @RequestMapping(path = "by/{field}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<Map<String, List<InfraService>>> getBy(@PathVariable String field, Authentication jwt) throws NoSuchFieldException {
        return ResponseEntity.ok(infraService.getBy(field));
    }

//    @Deprecated
//    @ApiOperation(value = "Get a past version of a specific infraService providing the infraService ID and a version identifier")
//    @RequestMapping(path = {"versions/{id}", "versions/{id}/{version}"}, method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
//    public ResponseEntity<List<InfraService>> versions(@PathVariable String id, @PathVariable Optional<String> version, Authentication jwt) throws ResourceNotFoundException {
//        return ResponseEntity.ok(infraService.versions(id, version.orElse(null)));
//    }
}
