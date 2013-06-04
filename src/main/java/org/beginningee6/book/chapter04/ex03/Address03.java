package org.beginningee6.book.chapter04.ex03;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Customer03エンティティにフィールドとして１対１にマッピングされる
 * 被所有側のエンティティクラス。
 * 
 * Customer03エンティティでPERSIST（永続化）とREMOVE（削除）操作に対して
 * カスケードが行われるように指定されているため、
 * それらの操作に関しては、Customer03エンティティに
 * 対する操作だけで、このエンティティに対する操作は必要なく、
 * 自動的に行われる。
 */
@Entity
@Table(name = "address_ex03")
public class Address03 implements Serializable {

	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;
    private String street1;
    private String city;
    private String zipcode;
    private String country;

    public Address03() {}

	public Address03(String street1, String city, String zipcode, String country) {
		this.street1 = street1;
		this.city = city;
		this.zipcode = zipcode;
		this.country = country;
	}

	public String getStreet1() {
		return street1;
	}

	public void setStreet1(String street1) {
		this.street1 = street1;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Address03 [id=" + id + ", street1=" + street1 + ", city="
				+ city + ", zipcode=" + zipcode + ", country=" + country + "]";
	}
}
