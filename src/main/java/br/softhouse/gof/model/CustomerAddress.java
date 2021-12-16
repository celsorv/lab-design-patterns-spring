package br.softhouse.gof.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * jsonschema2pojo.org
 * Generate Plain Old Java Objects from JSON or JSON-Schema.
 *
 * viacep.com.br
 * Query Brazil Postal Address Codes (CEP)
 *
 * @see <a href="https://www.jsonschema2pojo.org">jsonschema2pojo.org</a>
 * @see <a href="https://viacep.com.br">ViaCEP</a>
 *
 */

@Getter @Setter
@Entity
@Table(name = "customer_address")
public class CustomerAddress {

    @Id
    private String cep;

    private String logradouro;
    private String complemento;
    private String bairro;
    private String localidade;
    private String uf;
    private String ibge;
    private String gia;
    private String ddd;
    private String siafi;

}
