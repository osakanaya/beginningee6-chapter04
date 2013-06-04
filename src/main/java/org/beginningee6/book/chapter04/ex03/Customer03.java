package org.beginningee6.book.chapter04.ex03;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Address03エンティティをフィールドとして
 * 持つ所有側のエンティティクラス。
 * 
 * ＠OneToOneアノテーションにより、
 * Address03と１対１（一方向）でマッピングされ、
 * cascade属性でPERSIST（永続化）とREMOVE（削除）操作に対して
 * カスケードが行われるように指定されている。
 * 
 * この為、永続化と削除に関しては、このエンティティに
 * 対する操作だけで、Address03エンティティに対する操作は
 * 必要なく、自動的に行われる。
 */
@Entity
@Table(name = "customer_ex03")
public class Customer03 implements Serializable {

	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    @OneToOne(fetch = FetchType.LAZY, 
    		cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    		// カスケードの指定							
    @JoinColumn(name = "address_fk")
    private Address03 address;

    public Customer03() {}

	public Customer03(String firstName, String lastName, String email) {
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

	public Address03 getAddress() {
		return address;
	}

	public void setAddress(Address03 address) {
		this.address = address;
	}

	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Customer03 [id=" + id + ", firstName=" + firstName
				+ ", lastName=" + lastName + ", email=" + email + ", address="
				+ address + "]";
	}
}
