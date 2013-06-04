package org.beginningee6.book.chapter04.ex05;

import java.io.Serializable;

/**
 * 
 * JPQLの「SELECT NEW ...」文によりエンティティの一部のフィールドを
 * 検索結果として取得するときに、取得するフィールドを格納するための
 * オブジェクトを表現するクラス（＠Entityアノテーションは付与しない
 * ため、エンティティを表現するクラスではない）
 * 
 * 取得するエンティティのフィールドと１対１に対応するフィールドと
 * このフィールドに対するsetter/getterを作成する必要がある。
 * 
 * また、全てのフィールドを設定する為のコンストラクタを
 * 用意しておく必要がある。
 * 
 */
public class CustomerDTO05 implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String firstName;
    private String lastName;
    private String country;
	
    // 全てのフィールドを設定するコンストラクタが必要
    public CustomerDTO05(String firstName, String lastName, String country) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.country = country;
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

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
}
