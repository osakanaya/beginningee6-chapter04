package org.beginningee6.book.chapter04.ex02;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Address02エンティティをフィールドとして
 * 持つ所有側のエンティティクラス。
 * 
 * ＠OneToOneアノテーションにより、
 * Address02と１対１（一方向）のリレーションで関連付けられている。
 * 
 * また、 Customer02エンティティでPERSIST（永続化）やREMOVE（削除）操作に
 * 対するカスケードは指定されていないため、Customer02エンティティと関連づける
 * 形でAddress02エンティティを永続化したり削除したりする場合は、
 * 外部キー制約を考慮した順序でそれぞれのエンティティを明示的に永続化および
 * 削除する必要がある。
 * 
 */
@Entity
@Table(name = "customer_ex02")
public class Customer02 implements Serializable {

	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    @OneToOne
    @JoinColumn(name = "address_fk")
    private Address02 address;

    public Customer02() {}

	public Customer02(String firstName, String lastName, String email) {
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

	public Address02 getAddress() {
		return address;
	}

	public void setAddress(Address02 address) {
		this.address = address;
	}

	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Customer02 [id=" + id + ", firstName=" + firstName
				+ ", lastName=" + lastName + ", email=" + email + ", address="
				+ address + "]";
	}
}
