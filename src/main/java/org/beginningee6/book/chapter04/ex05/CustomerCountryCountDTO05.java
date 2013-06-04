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
public class CustomerCountryCountDTO05 implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String country;
    private Long count;

    // 全てのフィールドを設定するコンストラクタが必要
    public CustomerCountryCountDTO05(String country, Long count) {
		this.country = country;
		this.count = count;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}
}
