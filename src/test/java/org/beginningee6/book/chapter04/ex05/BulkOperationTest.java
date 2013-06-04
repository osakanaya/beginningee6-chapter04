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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * Customer05/Address05エンティティを対象としたJPQLのDELETE文、UPDATE文の
 * 動作確認テスト
 * 
 */
public class BulkOperationTest {
	private static final Logger logger = Logger.getLogger(BulkOperationTest.class.getName());
	
	private static EntityManagerFactory emf;
	private static EntityManager em;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		emf = Persistence.createEntityManagerFactory("test");
		em = emf.createEntityManager();
	}
	
	@Before
	public void setUp() throws Exception {
		clearData();
		persistData();
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
	
	private void clearData() throws Exception {
		EntityTransaction tx = em.getTransaction();		
		tx.begin();

		logger.info("Dumping old records...");
		em.createQuery("DELETE FROM Customer05").executeUpdate();
		em.createQuery("DELETE FROM Address05").executeUpdate();

		tx.commit();
	}
	
	/**
	 * それぞれのテストメソッド実行の前準備として、Customer05エンティティと
	 * Address05エンティティを生成し、Customer05のaddressフィールドにAddress05
	 * エンティティをセットしてエンティティを永続化しておく。
	 * 
	 * このエンティティの組を６個作成しデータベースに登録しておく。
	 */
	private void persistData() throws Exception {
		
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
	 * DELETE文によるエンティティの一括削除のテスト
	 */
	@Test
	public void testBulkDelete() throws Exception {
		
		///// 準備 /////
		
		// Customer05からageフィールドが18より小さいデータを削除するクエリを作成
		Query deleteQuery = em.createQuery("DELETE FROM Customer05 c WHERE c.age < 18");
		
		EntityTransaction tx = em.getTransaction();
		
		///// テスト /////
        
		tx.begin();
		// executeUpdate()によりDELETE文を実行
		int deleteCount = deleteQuery.executeUpdate();
		tx.commit();	// データベースへのDELETE文の発行はコミット時に行われる
        
		///// 検証 /////
		
		// 削除されたレコード数は2件
		assertThat(deleteCount, is(2));
		
		em.clear();
		
		// Customer05からageフィールドが18より小さいデータを検索するクエリを作成
		Query selectQuery = em.createQuery("SELECT c FROM Customer05 c WHERE c.age < 18");
		// getResultList()により検索を実行
		@SuppressWarnings("unchecked")		// 型が安全でないとの警告を抑える
		List<Customer05> customers = selectQuery.getResultList();
		
		// 検索結果は0件であること＝DELETE文により削除されたエンティティが
		// データベース上に存在しないことを確認する。
		assertThat(customers.size(), is(0));
	}
	
	/**
	 * UPDATE文によるエンティティの一括更新のテスト。
	 */
	@Test
	public void tesBulkUpdate() throws Exception {
		
		///// 準備 /////
		
		// Customer05からageフィールドが18より小さいデータの
		// firstNameフィールドを'TOO YOUNG'に更新
		Query updateQuery = em.createQuery("UPDATE Customer05 c SET c.firstName = 'TOO YOUNG' WHERE c.age < 18");
		
		EntityTransaction tx = em.getTransaction();
		
		///// テスト /////
        
		tx.begin();
		// executeUpdate()により更新を実行
		int updateCount = updateQuery.executeUpdate();
		tx.commit();
        
		///// 検証 /////
		
		// 更新されたレコード数は2件
		assertThat(updateCount, is(2));
		
		em.clear();
		
		// Customer05からageフィールドが18より小さいデータを検索するクエリを作成
		Query selectQuery = em.createQuery("SELECT c FROM Customer05 c WHERE c.age < 18");
		// getResultList()により検索を実行
		@SuppressWarnings("unchecked")
		List<Customer05> customers = selectQuery.getResultList();
		
		// 検索結果は2件
		assertThat(customers.size(), is(2));
		// 検索された2件のfirstNameフィールドが更新されている＝
		// UPDATE文によるエンティティのフィールド値の更新がデータベース
		// に反映されていることを確認する
		assertThat(customers.get(0).getFirstName(), is("TOO YOUNG"));
		assertThat(customers.get(1).getFirstName(), is("TOO YOUNG"));
	}
}
