package eu.einfracentral.domain;

import eu.einfracentral.annotation.FieldValidation;
import eu.einfracentral.annotation.VocabularyValidation;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;

@XmlType
@XmlRootElement(namespace = "http://einfracentral.eu")
public class ServiceProviderDomain {


    // Provider's Location Information
    /**
     * The branch of science, scientific discipline that is related to the Resource.
     */
    @XmlElement(required = true)
    @ApiModelProperty(position = 1, required = true)
    @FieldValidation(containsId = true, idClass = Vocabulary.class)
    @VocabularyValidation(type = Vocabulary.Type.SCIENTIFIC_DOMAIN)
    private String scientificDomain;

    /**
     * The subbranch of science, scientific subdicipline that is related to the Resource.
     */
    @XmlElement(required = true)
    @ApiModelProperty(position = 2, required = true)
    @FieldValidation(containsId = true, idClass = Vocabulary.class)
    @VocabularyValidation(type = Vocabulary.Type.SCIENTIFIC_SUBDOMAIN)
    private String scientificSubdomain;

    public ServiceProviderDomain() {
    }

    public ServiceProviderDomain(String scientificDomain, String scientificSubdomain) {
        this.scientificDomain = scientificDomain;
        this.scientificSubdomain = scientificSubdomain;
    }

    @Override
    public String toString() {
        return "ProviderDomains{" +
                "scientificDomain='" + scientificDomain + '\'' +
                ", scientificSubdomain='" + scientificSubdomain + '\'' +
                '}';
    }

    public String getScientificDomain() {
        return scientificDomain;
    }

    public void setScientificDomain(String scientificDomain) {
        this.scientificDomain = scientificDomain;
    }

    public String getScientificSubdomain() {
        return scientificSubdomain;
    }

    public void setScientificSubdomain(String scientificSubdomain) {
        this.scientificSubdomain = scientificSubdomain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceProviderDomain that = (ServiceProviderDomain) o;
        return Objects.equals(scientificDomain, that.scientificDomain) && Objects.equals(scientificSubdomain, that.scientificSubdomain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scientificDomain, scientificSubdomain);
    }
}
