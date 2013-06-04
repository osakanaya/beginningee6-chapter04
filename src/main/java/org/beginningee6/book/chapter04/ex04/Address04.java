package org.beginningee6.book.chapter04.ex04;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Customer04エンティティにフィールドとして１対１にマッピングされる
 * 被所有側のエンティティクラス。
 * 
 * Customer04エンティティ側で、データベース上にCustomer04エンティティ
 * と関係を持たないAddress04エンティティが残らないように指定されているため、
 * 以下のような操作を行ってデータベース上に関係の切れたAddress04エンティティ
 * が存在するする状態になるとコミット時のこのようなデータが自動的に削除される。
 * 
 * ・Customer04エンティティを削除する
 * ・Customer04エンティティのAddress04エンティティを参照するフィールドに
 * 　nullをセットしてCustomer04エンティティとAddress04エンティティの
 * 　関係を切る
 * ・Customer04エンティティのAddress04エンティティを参照するフィールドに
 * 　別のAddress04エンティティをセットして、元々参照されたAddress04
 * 　エンティティとCustomer04エンティティとの関係を切る
 * 
 */
@Entity
@Table(name = "address_ex04")
public class Address04 implements Serializable {

	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;
    private String street1;
    private String city;
    private String zipcode;
    private String country;

    public Address04() {}

	public Address04(String street1, String city, String zipcode, String country) {
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
		return "Address04 [id=" + id + ", street1=" + street1 + ", city="
				+ city + ", zipcode=" + zipcode + ", country=" + country + "]";
	}
}
