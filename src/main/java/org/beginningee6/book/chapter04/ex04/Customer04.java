package org.beginningee6.book.chapter04.ex04;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Address04エンティティをフィールドとして
 * 持つ所有側のエンティティクラス。
 * 
 * ＠OneToOneアノテーションにより、
 * Address04への１対１（一方向）のリレーションが
 * 設定され、かつ、orphanRemoval属性がtrueに指定されている。
 * 
 * このため、削除操作（EntityManager.remove)に関しては、
 * このエンティティに対する削除を行うだけで、参照される
 * Address03エンティティも自動的に削除される。
 * 
 * 加えて、Address03エンティティを参照するaddressフィールドに
 * nullがセットされたり、他のAddress03エンティティがセットされて
 * Customer03エンティティとの関係を何ら持たないAddress03エンティティ
 * に対しても削除が自動的に実行される。
 * 
 */
@Entity
@Table(name = "customer_ex04")
public class Customer04 implements Serializable {

	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    							// orphanRemoval属性をtrueに指定
    @JoinColumn(name = "address_fk")
    private Address04 address;

    public Customer04() {}

	public Customer04(String firstName, String lastName, String email) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Address04 getAddress() {
		return address;
	}

	public void setAddress(Address04 address) {
		this.address = address;
	}

	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Customer04 [id=" + id + ", firstName=" + firstName
				+ ", lastName=" + lastName + ", email=" + email + ", address="
				+ address + "]";
	}
}
