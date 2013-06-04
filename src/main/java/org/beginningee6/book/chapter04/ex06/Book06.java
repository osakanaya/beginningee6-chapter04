package org.beginningee6.book.chapter04.ex06;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * データベース更新における排他制御の
 * 仕組みとして＠Versionアノテーションを
 * 用いたバージョン管理を導入したエンティティ。
 * 
 * ＠Versionアノテーションが付与されたフィールド
 * （versionフィールド）におけるバージョン番号は
 * EntityManagerによって自動的に更新されるため、
 * versionフィールドのsetterを作成してはいけない。
 * 
 * データベースからエンティティのデータを取得する際に、
 * バージョン番号も取得される。
 * エンティティの操作後に、コミットを行うタイミングで、
 * データベース上のバージョンが変更されていなければ
 * 新しいバージョンと共にデータを更新する。
 * バージョンが変更されていれば、データがその間に
 * 他のトランザクションによって変更されていることになり、
 * コミットは失敗する。
 * 
 * バージョン番号を利用した他のトランザクションによる
 * データ更新有無のチェックはバージョン番号を持つフィールドに
 * ＠Versionアノテーションを付与する事により、自動的に行われる。
 * 
 */
@Entity
@Table(name = "book_ex06")
public class Book06 implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue
    private Long id;
	
    @Version
    private Integer version;	// このフィールドでバージョン管理を行う
    
    private String title;
    private Float price;
    private String description;
    private String isbn;
    private Integer nbOfPage;
    private Boolean illustrations;

    public Book06() {}

    // コンストラクタでもバージョン番号を初期化してはいけない
	public Book06(String title, Float price, String description, String isbn,
			Integer nbOfPage, Boolean illustrations) {
		this.title = title;
		this.price = price;
		this.description = description;
		this.isbn = isbn;
		this.nbOfPage = nbOfPage;
		this.illustrations = illustrations;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public Integer getNbOfPage() {
		return nbOfPage;
	}

	public void setNbOfPage(Integer nbOfPage) {
		this.nbOfPage = nbOfPage;
	}

	public Boolean getIllustrations() {
		return illustrations;
	}

	public void setIllustrations(Boolean illustrations) {
		this.illustrations = illustrations;
	}

	public Long getId() {
		return id;
	}

	// バージョン番号（versionフィールド）にはgetterのみ用意すること。
	public Integer getVersion() {
		return version;
	}

    public void raisePriceByTwoDollars() {
        price = price + 2;
    }

    public void raisePriceByFiveDollars() {
        price = price + 5;
    }

    @Override
	public String toString() {
		return "Book06 [id=" + id + ", version=" + version + ", title=" + title
				+ ", price=" + price + ", description=" + description
				+ ", isbn=" + isbn + ", nbOfPage=" + nbOfPage
				+ ", illustrations=" + illustrations + "]";
	}
}
