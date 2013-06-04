package org.beginningee6.book.chapter04.ex03;

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
import org.junit.Test;

/**
 * 
 * EntityManagerを使用した、リレーションを持つエンティティの永続化と
 * 削除に関するテスト。
 * 
 * 操作対象のエンティティとしてCustomer03エンティティとAddress03
 * エンティティを使用する。
 * 
 * Address03エンティティはCustomer03エンティティに１対１でマップ
 * されており、PERSIST（永続化）とREMOVE（削除）に対してカスケード
 * 指定がされている。
 * 
 * この場合、PERSIST（永続化）とREMOVE（削除）操作に対しては
 * Customer03エンティティに対して操作を行うだけで、対応する
 * Address03エンティティにも連鎖的に操作が行われる。
 * 
 */
public class Customer03Test {
	
	private static final Logger logger = Logger.getLogger(Customer03Test.class.getName());
	
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
		em.createQuery("DELETE FROM Customer03").executeUpdate();
		
		tx.commit();
	}
	
	/**
	 * 
	 * エンティティ間のリレーションにPERSIST（永続化）とREMOVE（削除）
	 * 操作に対するカスケードが行われるように指定されている場合での、
	 * エンティティの永続化に関するテスト。
	 * 
	 * ・Customer03エンティティを生成後、Address03エンティティを生成し、
	 * 　Customer03エンティティのaddressフィールドにセットする。
	 * ・Customer03エンティティのみ永続化し、コミットする。
	 * 
	 */
	@Test
	public void testCascadeForPersistEvents() throws Exception {
		
		///// 準備 /////
		
        Customer03 customer = new Customer03("Antony", "Balla", "tballa@mail.com");
        Address03 address = new Address03("Ritherdon Rd", "London", "8QE", "UK");
        customer.setAddress(address);	// addressをcustomerにセット
        
		///// テスト /////
		
        EntityTransaction tx = em.getTransaction();
        
        tx.begin();

        //em.persist(address);	// Address03エンティティはCustomer03の永続化に
								// よって連鎖的に永続化されるように設定されている
								// ため、明示的な永続化は不要（行っても問題ない）

        em.persist(customer);	// Customer03エンティティのみ永続化

        tx.commit();			// もし、連鎖的に永続化されるように設定されていない
        						// 場合で、Address03エンティティの永続化を忘れると
        						// コミットで例外発生する

		///// 検証 /////
		
        // Customer03エンティティだけでなく、Address03エンティティも
        // 連鎖的に永続化されたことをIDの付番によって確認
        assertThat(customer.getId(), is(notNullValue()));
        assertThat(address.getId(), is(notNullValue()));
        
        // 永続化された2つのエンティティがEntityManagerの管理対象である
        // ことを確認する
        assertThat(em.contains(customer), is(true));
        assertThat(em.contains(address), is(true));
        
        em.clear();		// データを確実にデータベースから取得するため、
        				// 全てのエンティティをエンティティマネージャの
        				// 管理対象外とする
        
        // データベースからidをキーにCustomer03エンティティを取得
        Customer03 persistedCustomer = em.find(Customer03.class, customer.getId());
        // 取得したCustomer03エンティティからAddress03エンティティを取得
        Address03 persistedAddress = persistedCustomer.getAddress();

        // IDの付番によってCustomer03エンティティが永続化されたことを確認
        assertThat(persistedCustomer, is(notNullValue()));
        assertThat(persistedCustomer.getId(), is(customer.getId()));

        // IDの付番によってAddress03エンティティが永続化されたことを確認
        assertThat(persistedAddress, is(notNullValue()));
        assertThat(persistedAddress.getId(), is(address.getId()));
	}
	
	/**
	 * 
	 * エンティティ間のリレーションにPERSIST（永続化）とREMOVE（削除）
	 * 操作に対するカスケードが行われるように指定されている場合での、
	 * エンティティの削除に関するテスト。
	 * 
	 * ・Customer03エンティティを生成後、Address03エンティティを生成し、
	 * 　Customer03エンティティのaddressフィールドにセットする。
	 * ・Customer03エンティティのみ永続化し、コミットする。
	 * ・その後、Customer03エンティティのみに対して削除操作を行い、
	 * 　Address03エンティティも削除されることを確認する。
	 * 
	 */

	@Test
	public void testCascadeForRemoveEvent() throws Exception {
		
		///// 準備 /////
		
        Customer03 customer = new Customer03("Antony", "Balla", "tballa@mail.com");
        Address03 address = new Address03("Ritherdon Rd", "London", "8QE", "UK");
        customer.setAddress(address);	// addressをcustomerにセット
        
        EntityTransaction tx = em.getTransaction();
        
        tx.begin();
        em.persist(customer);	// Customer02エンティティを永続化
        tx.commit();			// カスケード対象でなければ、コミットで例外発生
		
		///// テスト /////
		
        tx.begin();
        em.remove(customer);	// Customer03エンティティを削除
        //em.remove(address);	// Address03エンティティはCustomer03の削除に
								// 伴い連鎖的に削除されるように設定されている
        						// ため、明示的な削除は不要
        tx.commit();
        
		///// 検証 /////
        em.clear();
		
        // データベースからidをキーにCustomer03エンティティを取得
        Customer03 persistedCustomer = em.find(Customer03.class, customer.getId());
        // データベースからidをキーにAddress03エンティティを取得
        Address03 persistedAddress = em.find(Address03.class, address.getId());
        
        // Customer03のデータが削除されたことを確認する
        // （削除されたエンティティは、em.find()によってnullが返される）
        assertThat(persistedCustomer, is(nullValue()));
        
        // Address03のデータも削除されたことを確認する
        
        // もしREMOVE（削除）操作に対するカスケードが指定されてなかったと
        // すると、em.remove(customer)を実行してもデータは削除されないため、
        // em.find()によってエンティティが返されるため、以下のassertは失敗
        // するはずである。
        assertThat(persistedAddress, is(nullValue()));
	}
	
	/**
	 * 
	 * リレーションシップにより参照関係を持つ2つのエンティティの
	 * 削除した場合の挙動を確認するテスト。
	 * 
	 * ・Customer03エンティティを生成後、Address03エンティティを生成し、
	 * 　Customer03エンティティにマップする。
	 * ・Customer03エンティティのみ永続化しコミット。
	 * 
	 * ・その後、Customer03エンティティのaddressフィールドにnullを設定しコミット。
	 * 　これで、Javaオブジェクトとデータベースの両方で２つのエンティティの関係が
	 * 　切れる。
	 * ・その後、Customer02エンティティのみを削除。
	 * ・＠OneToOneアノテーションのorphanRemoval属性がtrueに
	 * 　設定されていないため、関係が切れたAddress02エンティティは、
	 * 　REMOVE操作に対してカスケードを指定したとしても削除されない。
	 * 
	 */
	@Test
	public void testMakeEntityOrphanBySettingNullToReference() throws Exception {
		
		///// 準備 /////
		
        Customer03 customer = new Customer03("Antony", "Balla", "tballa@mail.com");
        Address03 address = new Address03("Ritherdon Rd", "London", "8QE", "UK");
        customer.setAddress(address);	// addressをcustomerにセット
        
        EntityTransaction tx = em.getTransaction();
        
        tx.begin();
        em.persist(customer);	// Customer03,Address03エンティティを永続化
        tx.commit();
		
		///// テスト1：エンティティ間の関係を切る /////
        
        tx.begin();
        customer.setAddress(null);	// addressプロパティにnullをセット
        tx.commit();        		// これによりエンティティ間の関係が切れる

        ///// 検証 /////
        em.clear();

        // データベースからidをキーにCustomer02エンティティを取得
        Customer03 persistedCustomer = em.find(Customer03.class, customer.getId());
        // データベースからidをキーにAddress02エンティティを取得
        Address03 persistedAddress = em.find(Address03.class, address.getId());

        // Customer03エンティティはデータベース上から削除されていない
        assertThat(persistedCustomer, is(notNullValue()));
        // Address03エンティティはデータベース上から削除されていない
        assertThat(persistedAddress, is(notNullValue()));
        // Customer03エンティティからAddress03エンティティを取得できない
        assertThat(persistedCustomer.getAddress(), is(nullValue()));
        
		///// テスト2：Customer02エンティティを削除する /////

        tx.begin();        
        em.remove(persistedCustomer);      	// Customer02エンティティのみを削除         
        tx.commit();
        
		///// 検証 /////
        em.clear();
		
        // データベースからidをキーにCustomer02エンティティを取得
        persistedCustomer = em.find(Customer03.class, customer.getId());
        // データベースからidをキーにAddress02エンティティを取得
        persistedAddress = em.find(Address03.class, address.getId());
        
        // Customer03エンティティが削除されている
        assertThat(persistedCustomer, is(nullValue()));
        
        // REMOVEはカスケード対象だが、
        // Customer03のaddressフィールドがnullに設定され、
        // Address03エンティティとの関係が切れているため、
        // Address03エンティティはデータベース上からは削除されていない
        assertThat(persistedAddress, is(notNullValue()));
	}
	
	/**
	 * 
	 * リレーションシップにより参照関係を持つ2つのエンティティの
	 * うち、片方のエンティティを別のエンティティに付け替えた場合の挙動を
	 * 確認するテスト。
	 * 
	 * ・Customer03エンティティを生成後、Address03エンティティを生成し、
	 * 　Customer03エンティティにマップする。
	 * ・Customer03エンティティのみ永続化しコミット。
	 * 
	 * ・その後、別のAddress03エンティティを生成し、Customer03エンティティの
	 * 　addressフィールドにそのエンティティへの参照を設定しコミット。
	 * 　これで、Javaオブジェクトとデータベースの両方でCustomer03エンティティと
	 * 　最初に生成したAddress03エンティティとの関係が切れる。
	 * ・＠OneToOneアノテーションのorphanRemoval属性がtrueに
	 * 　設定されていないため、関係が切れたAddress02エンティティは、
	 * 　REMOVE操作に対してカスケードを指定したとしても削除されない。
	 */
	@Test
	public void testMakeEntityOrphanBySettingAnotherEntityToReference() throws Exception {
		
		///// 準備 /////
		
        Customer03 customer = new Customer03("Antony", "Balla", "tballa@mail.com");
        Address03 address1 = new Address03("Ritherdon Rd", "London", "8QE", "UK");
        customer.setAddress(address1);	// addressをcustomerにセット
        
        EntityTransaction tx = em.getTransaction();
        
        tx.begin();
        em.persist(customer);	// Customer03,Address03エンティティを永続化
        tx.commit();
		
		///// テスト /////
        
        // 別のAddress03エンティティを生成
        Address03 address2 = new Address03("Abbey Rd", "London", "8QE", "UK");
        
        tx.begin();
        em.persist(address2);
        customer.setAddress(address2);	// addressプロパティにaddress2をセット
        tx.commit();        			// これによりaddress1との関係が切れる
		
		///// 検証 /////
        em.clear();
		
        // データベースからidをキーにCustomer03エンティティを取得
        Customer03 persistedCustomer = em.find(Customer03.class, customer.getId());
        // Customer03エンティティから参照されるAddress02エンティティを取得
        Address03 referencedAddress = persistedCustomer.getAddress();

        //後に生成したAddress03エンティティがCustomer03から参照されている
        assertThat(referencedAddress.getId(), is(address2.getId()));
        
        // データベースからidをキーにAddress03エンティティを取得
        Address03 persistedAddress1 = em.find(Address03.class, address1.getId());
        Address03 persistedAddress2 = em.find(Address03.class, address2.getId());
       
        // Customer03エンティティとの関係は切れていても最初に生成した
        // Address03エンティティは削除されずに残ったままとなる
        assertThat(persistedAddress1, is(notNullValue()));
        assertThat(persistedAddress2, is(notNullValue()));
	}
}
