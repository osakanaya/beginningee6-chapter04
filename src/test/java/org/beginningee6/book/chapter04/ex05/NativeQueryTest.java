package org.beginningee6.book.chapter04.ex05;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JPQLのネイティブクエリによるエンティティ検索のテスト。
 * 
 * ネイティブクエリでは、データベース上の実際のテーブル名や
 * カラム名でクエリを構成する。
 * 
 * なお、サポートされるSQL文はデータベースサーバの種類ごとに
 * 異なる（方言があるため）、あるデータベースサーバ上で
 * 動作するネイティブクエリが別にデータベースサーバでも
 * 動作する保証が無いことに注意すること。
 * 
 */
public class NativeQueryTest {
	
	private static final Logger logger = Logger.getLogger(NativeQueryTest.class.getName());
	
	private static EntityManagerFactory emf;
	private static EntityManager em;

	@BeforeClass
	public static void setUpClass() throws Exception {
		emf = Persistence.createEntityManagerFactory("test");
		em = emf.createEntityManager();

		clearData(em);
		
		persistData(em);
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception {
		if (em != null) {
			em.close();
		}
		
		if (emf != null) {
			emf.close();
		}
	}

	private static void clearData(EntityManager em) throws Exception {
		EntityTransaction tx = em.getTransaction();		
		tx.begin();

		logger.info("Dumping old records...");
		em.createQuery("DELETE FROM Customer05").executeUpdate();
		em.createQuery("DELETE FROM Address05").executeUpdate();

		tx.commit();
	}

	/**
	 * 各テスト前準備として、Customer05エンティティとAddress05エンティティを
	 * 生成し、Customer05のaddressフィールドにAddress05エンティティをセットする。
	 * 
	 * このエンティティの組を6個作成しデータベースに登録しておく。
	 */
	private static void persistData(EntityManager em) throws Exception {
        Customer05 customer01 = new Customer05("Antony", "Balla", "tballa@mail.com", 14);
        Address05 address01 = new Address05("Procession St", "Paris", "75015", "FR");
        customer01.setAddress(address01);

        Customer05 customer02 = new Customer05("Vincent", "Johnson", "vj@mail.com", 45);
        Address05 address02 = new Address05("Ritherdon Rd", "London", "8QE", "UK");
        customer02.setAddress(address02);

        Customer05 customer03 = new Customer05("Sebastian", "Twenty", "seb@yamail.com", 58);
        Address05 address03 = new Address05("Inacio Alfama", "Lisbon", "A54", "PT");
        customer03.setAddress(address03);

        Customer05 customer04 = new Customer05("Frederic", "Riou", "fred@carmail.com", 41);
        Address05 address04 = new Address05("Jardins", "Sao Paulo", "345678", "BR");
        customer04.setAddress(address04);

        Customer05 customer05 = new Customer05("Vincent", "Dubosc", "vd@yahoo.com", 14);
        Address05 address05 = new Address05("Coffey", "Perth", "654F543", "AU");
        customer05.setAddress(address05);

        Customer05 customer06 = new Customer05("David", "Chene", "dch@yahoo.com", 89);
        Address05 address06 = new Address05("Harbour Bridge", "Sydney", "JHG3", "AU");
        customer06.setAddress(address06);

        EntityTransaction tx = em.getTransaction();		

        tx.begin();
        
        em.persist(customer01);
        em.persist(customer02);
        em.persist(customer03);
        em.persist(customer04);
        em.persist(customer05);
        em.persist(customer06);
        
        tx.commit();
	}
	
	/**
	 * ネイティブクエリのテスト。
	 * 
	 * FROM句にはデータベース上のテーブル名を指定する。
	 * 
	 * ・＠Tableアノテーションを使用してエンティティがマッピングされるテーブル名を
	 * 　明示的に指定していない場合は、テーブル名とエンティティ名は同じ。
	 * ・＠Tableアノテーションを使用している場合は、name属性で指定したテーブル名を
	 * 　FROM句に指定する。
	 * 
	 * ここの例では、＠Tableアノテーションで指定されているテーブル名＝customer_ex05
	 * をFROM句に指定している。
	 * 
	 */
	@Test
	public void testNativeQuery_1() throws Exception {
		
		///// 準備 /////
		
		// createNativeQueryでネイティブクエリを作成する
		Query query = em.createNativeQuery("SELECT * FROM customer_ex05");
		
		///// テスト /////
        
		@SuppressWarnings("unchecked")
		List<Customer05> customers = query.getResultList();
        
		///// 検証 /////
		
		assertThat(customers.size(), is(6));
	}
	
	/**
	 * ネイティブクエリのテスト。
	 * 
	 * SELECT句にはデータベース上のカラム名を指定する。
	 * 
	 * ・＠Columnアノテーションを使用してエンティティのフィールドがマッピングされる
	 * 　カラム名を明示的に指定していない場合は、カラム名とフィールド名は同じ。
	 * ・＠Columnアノテーションを使用している場合は、name属性で指定したカラム名を
	 * 　SELECT句に指定する。
	 * 
	 * ここの例では、＠Columnアノテーションで指定されているカラム名＝first_name
	 * をSELECT句に指定している。
	 * 
	 */
	@Test
	public void testNativeQuery_2() throws Exception {
		
		///// 準備 /////
		
		// createNativeQueryでネイティブクエリを作成する
		Query query = em.createNativeQuery("SELECT first_name FROM customer_ex05");
		
		///// テスト /////
        
		@SuppressWarnings("unchecked")
		List<String> firstNames = query.getResultList();
        
		///// 検証 /////
		
		assertThat(firstNames.size(), is(6));
	}
	
	/**
	 * ネイティブクエリのテスト。
	 * 
	 * エンティティ間をリレーションによって関連付ける場合に
	 * おいて、＠JoinColumnアノテーションで明示的に結合列の
	 * カラム名をname属性で指定している場合は、そのカラム名を
	 * 使って結合のSQL文を作成する。
	 * 
	 * ＠JoinColumnアノテーションを使用していない場合は、
	 * フィールド名＋"_ID"を結合列のカラム名として使用する。
	 * 
	 * ここの例では、＠JoinColumnアノテーションで指定されている
	 * 結合列のカラム名＝address_fkを結合条件のフィールド名で
	 * 指定している。
	 * 
	 */
	@Test
	public void testNativeQuery_3() throws Exception {
		
		///// 準備 /////
		
		// createNativeQueryでネイティブクエリを作成する
		Query query = em.createNativeQuery(
				"SELECT c.first_name, c.lastname, a.country " +
				"FROM customer_ex05 c " +
				"JOIN address_ex05 a " +
				"ON c.address_fk = a.id");
		
		///// テスト /////
        
		@SuppressWarnings("unchecked")
		List<Address05> addresses = query.getResultList();
        
		///// 検証 /////
		
		assertThat(addresses.size(), is(6));
	}
}
