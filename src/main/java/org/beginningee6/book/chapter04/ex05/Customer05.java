package org.beginningee6.book.chapter04.ex05;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Address05エンティティをフィールドとして
 * 持つ所有側のエンティティクラス。
 * 
 * ＠NamedQueriesと＠NamedQueryアノテーションにより、
 * 4つの名前付きクエリが定義されている。
 * 
 * "findAll" 			Customer05からデータを全て取得する。
 * Customer05.FIND_ALL	Customer05からデータを全て取得する（定数文字列使用）。
 * "findVincent"		Customer05からfirstNameが'Vincent'のデータを全て取得する。
 * "findWithParam"		Customer05からfirstNameがパラメータで指定された値のデータを全て取得する。
 * 
 * また、＠OneToOneアノテーションにより、
 * Address05エンティティへの１対１（一方向）のリレーションが設定されており、cascade属性で
 * PERSIST（永続化）とREMOVE（削除）操作に対してカスケードが行われるように指定されている。
 */
@Entity
@Table(name = "customer_ex05")
@NamedQueries({		// 名前付きクエリの定義
        @NamedQuery(name = "findAll", query = "SELECT c FROM Customer05 c"),
        @NamedQuery(name = Customer05.FIND_ALL, query = "SELECT c FROM Customer05 c"),
        @NamedQuery(name = "findVincent", query = "SELECT c FROM Customer05 c WHERE c.firstName = 'Vincent'"),
        @NamedQuery(name = "findWithParam", query = "SELECT c FROM Customer05 c WHERE c.firstName = :fname")
})
public class Customer05 implements Serializable {

    public static final String FIND_ALL = "Customer.findAll";

    private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue
    private Long id;
	@Column(name = "first_name")
    private String firstName;
    private String lastName;
    private Integer age;
    private String email;
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name = "address_fk")
    private Address05 address;

    public Customer05() {}

	public Customer05(String firstName, String lastName, String email, 
			Integer age) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
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

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Address05 getAddress() {
		return address;
	}

	public void setAddress(Address05 address) {
		this.address = address;
	}

	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Customer05 [id=" + id + ", firstName=" + firstName
				+ ", lastName=" + lastName + ", age=" + age + ", email="
				+ email + ", address=" + address + "]";
	}
}
