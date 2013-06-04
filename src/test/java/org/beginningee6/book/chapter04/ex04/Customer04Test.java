package org.beginningee6.book.chapter04.ex04;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * EntityManagerを使用した、リレーションを持つエンティティの永続化と
 * 削除に関するテスト。
 * 
 * 操作対象のエンティティとしてCustomer04エンティティとAddress04
 * エンティティを使用する。
 * 
 * Address04エンティティはCustomer04エンティティに１対１でマップ
 * されており、orphanRemovalオプションが有効になっている。
 * 
 * この場合、削除操作に対してはCustomer04の削除を行うだけで、関係する
 * Address04エンティティも連鎖的に自動で削除が行われる。 
 * 
 */
public class Customer04Test {
	
	private static final Logger logger = Logger.getLogger(Customer04Test.class.getName());
	
	private static EntityManagerFactory emf;
	private static EntityManager em;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		emf = Persistence.createEntityManagerFactory("test");
		em = emf.createEntityManager();
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
	
	@Before
	public void setUp() throws Exception {
		clearData();
	}
	
	private void clearData() throws Exception {
		EntityTransaction tx = em.getTransaction();		
		tx.begin();

		logger.info("Dumping old records...");
		em.createQuery("DELETE FROM Customer04").executeUpdate();
		
		tx.commit();
	}
	
	/**
	 * 
	 * エンティティ間のリレーションにorphanRemovalオプションが
	 * 指定されている場合でのエンティティの削除に関するテスト。
	 * 
	 * ・Customer04エンティティを生成後、Address04エンティティを生成し、
	 * 　Customer04エンティティのaddressフィールドにセットする。
	 * ・Customer04,Address04エンティティを永続化し、コミットする。
	 * ・その後、Customer04エンティティのみに対して削除操作を行い、
	 * 　Address04エンティティも削除されることを確認する。
	 * 
	 */
	@Test
	public void testEntityRemovalByOrphanRemoval() throws Exception {
		
		///// 準備 /////
		
        Customer04 customer = new Customer04("Antony", "Balla", "tballa@mail.com");
        Address04 address = new Address04("Ritherdon Rd", "London", "8QE", "UK");
        customer.setAddress(address);	// addressをcustomerにセット
        
        EntityTransaction tx = em.getTransaction();
        
        tx.begin();
        em.persist(address);
        em.persist(customer);
        tx.commit();
		
		///// テスト /////
        
        tx.begin();
        em.remove(customer);	// Customer04エンティティのみを削除
        tx.commit();
        
		///// 検証 /////
		
        em.clear();
        
        // データベースからidをキーにCustomer04エンティティを取得
        Customer04 persistedCustomer = em.find(Customer04.class, customer.getId());
        // データベースからidをキーにAddress04エンティティを取得
        Address04 persistedAddress = em.find(Address04.class, address.getId());
        
        // Customer03エンティティのデータが削除されている
        assertThat(persistedCustomer, is(nullValue()));
        
        // Address03エンティティのデータも削除されている
        assertThat(persistedAddress, is(nullValue()));
	}

	/**
	 * 
	 * エンティティ間のリレーションにorphanRemovalオプションが
	 * 指定されている場合でのエンティティの削除に関するテスト。
	 * 
	 * ・Customer04エンティティを生成後、Address04エンティティを生成し、
	 * 　Customer04エンティティのaddressフィールドにセットする。
	 * ・Customer04,Address04エンティティを永続化し、コミットする。
	 * ・その後、Customer04エンティティのaddressフィールドにnullを
	 * 　を設定し、Customer04エンティティとAddress04エンティティの
	 * 　関係を切る。
	 * ・orphanRemovalオプションにより、Address04エンティティが削除
	 * 　されることを確認する。
	 * 
	 */
	@Ignore("JPA実装であるHibernateのバグにより、Address04エンティティのデータが削除されないため、このテストを無効化しています。")
	@Test
	public void testRemoveOrphanBySettingNull() throws Exception {
		
		///// 準備 /////
		
        Customer04 customer = new Customer04("Antony", "Balla", "tballa@mail.com");
        Address04 address = new Address04("Ritherdon Rd", "London", "8QE", "UK");
        customer.setAddress(address);	// addressをcustomerにセット
        
        EntityTransaction tx = em.getTransaction();
        
        tx.begin();
        em.persist(customer);
        em.persist(address);
        tx.commit();
		
		///// テスト /////
        
        tx.begin();
        customer.setAddress(null);	// addressプロパティにnullをセット
        tx.commit();        		// これによりエンティティ間の関係が切れる
		
		///// 検証 /////
        em.clear();
		
        // データベースからidをキーにCustomer04エンティティを取得
        Customer04 persistedCustomer = em.find(Customer04.class, customer.getId());
        // データベースからidをキーにAddress04エンティティを取得
        Address04 persistedAddress = em.find(Address04.class, address.getId());
        
        // Customer04のデータは存在する
        assertThat(persistedCustomer, is(notNullValue()));
        
        // Customer04エンティティのaddressフィールドがnullに設定され、
        // Address04エンティティとの関係は切れているが、
        // orphanRemoval属性がtrueに指定されているため、
        // Address04エンティティが削除されていることを確認する
        assertThat(persistedAddress, is(nullValue()));
	}
	
	/**
	 * 
	 * エンティティ間のリレーションにorphanRemovalオプションが
	 * 指定されている場合でのエンティティの削除に関するテスト。
	 * 
	 * ・Customer04エンティティを生成後、Address04エンティティを生成し、
	 * 　Customer04エンティティのaddressフィールドにセットする。
	 * ・Customer04,Address04エンティティを永続化し、コミットする。
	 * ・その後、別のAddress04エンティティを生成・永続化し、Customer04
	 * 　エンティティのaddressフィールドにこのエンティティをセット。
	 * 　これにより、最初に生成したAddress04エンティティとCustomer04
	 * 　エンティティとの関係を切る。
	 * ・orphanRemovalオプションにより、最初のAddress04エンティティが
	 * 　削除されることを確認する。
	 * 
	 */
	@Ignore("JPA実装であるHibernateのバグにより、Address04エンティティのデータが削除されないため、このテストを無効化しています。")
	@Test
	public void testRemoveOrphanBySettingAnotherReference() throws Exception {
		
		///// 準備 /////
		
        Customer04 customer = new Customer04("Antony", "Balla", "tballa@mail.com");
        Address04 address1 = new Address04("Ritherdon Rd", "London", "8QE", "UK");
        customer.setAddress(address1);	// addressをcustomerにセット
        
        EntityTransaction tx = em.getTransaction();
        
        tx.begin();
        em.persist(customer);
        em.persist(address1);
        tx.commit();
		
		///// テスト /////
        
        // 別のAddress04エンティティを生成
        Address04 address2 = new Address04("Abbey Rd", "London", "8QE", "UK");
        
        tx.begin();
        em.persist(address2);
        customer.setAddress(address2);	// addressプロパティにaddress2をセット
        tx.commit();        			// これによりaddress1との関係が切れる
		
		///// 検証 /////
        em.clear();
		
        // データベースからidをキーにCustomer04エンティティを取得
        Customer04 persistedCustomer = em.find(Customer04.class, customer.getId());
        // データベースからidをキーに最初に生成したAddress04エンティティを取得
        Address04 persistedAddress1 = em.find(Address04.class, address1.getId());
        // データベースからidをキーに2番目に生成したAddress04エンティティを取得
        Address04 persistedAddress2 = em.find(Address04.class, address2.getId());
        
        // Customer02エンティティが参照しているのは2番目に生成したAddress04エンティティ
        assertThat(persistedCustomer.getAddress().getId(), is(persistedAddress2.getId()));
        
        // Customer04のaddressフィールドに他の参照がセットされ、
        // address1との関係は切れているが、
        // orphanRemoval属性がtrueに指定されているため、
        // address1エンティティは削除されていることを確認する
        assertThat(persistedAddress1, is(nullValue()));
	}
}
