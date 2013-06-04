package org.beginningee6.book.chapter04.ex02;

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
 * EntityManagerを使用したエンティティの様々な操作例を示すテスト。
 * 
 * 操作対象のエンティティとしてCustomer02エンティティとAddress02
 * エンティティを使用する。
 * 
 * Address02エンティティとCustomer02エンティティはCustomer02エンティティ
 * からAddress02エンティティへの１対１のリレーションシップで関連付け
 * られているが、永続化や削除操作に関するカスケード指定はされていない。
 * 
 * この場合、永続化や削除操作においては、Customer02エンティティだけでなく、
 * このエンティティに関連づけられるAddress02エンティティも明示的に永続化や
 * 削除を適切な順序で行う必要がある。
 * 
 */
public class Customer02Test {
	
	private static final Logger logger = Logger.getLogger(Customer02Test.class.getName());
	
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
		em.createQuery("DELETE FROM Customer02").executeUpdate();
		em.createQuery("DELETE FROM Address02").executeUpdate();
		
		tx.commit();
	}
	
	/**
	 * 
	 * エンティティ間のリレーションに永続化や削除操作に対するカスケード
	 * が指定されていない場合での、エンティティの永続化に関するテスト。
	 * 
	 * この場合、Customer02エンティティがAddress02エンティティを参照する
	 * 状態でエンティティの永続化を行う場合は、両方のエンティティを
	 * 永続化し、かつ、Address02エンティティを先に永続化する必要がある。
	 * 
	 * ・Customer02エンティティを生成後、Address02エンティティを生成し、
	 * 　Customer02エンティティのaddressフィールドにセットする。
	 * ・先にAddress02エンティティを永続化する。
	 * ・次にCustomer02エンティティを永続化し、コミットする。
	 * 
	 */
	@Test
	public void testPersistCustomerAndAddress() throws Exception {
		
		///// 準備 /////
		
        Customer02 customer = new Customer02("Antony", "Balla", "tballa@mail.com");
        Address02 address = new Address02("Ritherdon Rd", "London", "8QE", "UK");
        customer.setAddress(address);	// addressをcustomerにセット
        
		///// テスト /////
		
        EntityTransaction tx = em.getTransaction();
        
        tx.begin();

        em.persist(address);	// customerが参照するAddress02エンティティを先に永続化する
        
        em.persist(customer);	// 次にCustomer02エンティティを永続化

        tx.commit();

		///// 検証 /////
		
        // Customer02エンティティと、これに関連するAddress02エンティティ
        // の両方が永続化されたことをIDの付番によって確認
        assertThat(customer.getId(), is(notNullValue()));
        assertThat(address.getId(), is(notNullValue()));
        
        // 永続化された2つのエンティティがEntityManagerの管理対象である
        // ことを確認する
        assertThat(em.contains(customer), is(true));
        assertThat(em.contains(address), is(true));
        
        em.clear();		// データを確実にデータベースから取得するため、
        				// 全てのエンティティをエンティティマネージャの
        				// 管理対象外とする
        
        // データベースからidをキーにCustomer02エンティティを取得
        Customer02 persistedCustomer = em.find(Customer02.class, customer.getId());
        // 取得したCustomer02エンティティからAddress02エンティティを取得
        Address02 persistedAddress = persistedCustomer.getAddress();

        // IDの付番によってCustomer02エンティティが永続化されたことを確認
        assertThat(persistedCustomer, is(notNullValue()));
        assertThat(persistedCustomer.getId(), is(customer.getId()));

        // IDの付番によってAddress02エンティティが永続化されたことを確認
        assertThat(persistedAddress, is(notNullValue()));
        assertThat(persistedAddress.getId(), is(address.getId()));
	}
	
	/**
	 * 
	 * エンティティ間のリレーションに永続化や削除操作に対するカスケード
	 * が指定されていない場合での、エンティティの削除に関するテスト。
	 * 
	 * この場合、Customer02エンティティがAddress02エンティティを参照する
	 * 状態でエンティティの削除を行う場合は、両方のエンティティを
	 * 削除し、かつ、参照する側であるCustomer02エンティティを先に削除する
	 * 必要がある。
	 * 
	 * ・Customer02エンティティを生成後、Address02エンティティを生成し、
	 * 　Customer02エンティティのaddressフィールドにセットする。
	 * ・先にAddress02エンティティを、次にCustomer02エンティティを
	 * 　永続化し、コミットする。
	 * 
	 * ・Customer02エンティティを先に削除、次に、Address02エンティティを
	 * 　削除し、データベース上から対応するレコードが削除されることを確認する。
	 * 
	 */
	@Test
	public void testRemoveCustomerAndAddress() throws Exception {
		
		///// 準備 /////
		
        Customer02 customer = new Customer02("Antony", "Balla", "tballa@mail.com");
        Address02 address = new Address02("Ritherdon Rd", "London", "8QE", "UK");
        customer.setAddress(address);	// addressをcustomerにセット
        
        EntityTransaction tx = em.getTransaction();
        
        tx.begin();
        
        em.persist(address);	// 先にAddress02エンティティを永続化
        em.persist(customer);	// Customer02エンティティを永続化

        tx.commit();			
		
		///// テスト /////
		
        tx.begin();
        
        em.remove(customer);	// Customer02エンティティ（参照する側）を先に削除
        
        em.remove(address);		// Address02エンティティ（参照される側）を次に削除
        
        tx.commit();
        
		///// 検証 /////
        em.clear();
		
        // データベースからidをキーにCustomer02エンティティを取得
        Customer02 persistedCustomer = em.find(Customer02.class, customer.getId());
        // データベースからidをキーにAddress02エンティティを取得
        Address02 persistedAddress = em.find(Address02.class, address.getId());
        
        // Customer02のデータが削除されたことを確認する
        // （削除されたエンティティは、em.find()によってnullが返される）
        assertThat(persistedCustomer, is(nullValue()));
        
        // Address02のデータも削除されたことを確認する
        assertThat(persistedAddress, is(nullValue()));
	}
	
	/**
	 * 
	 * トランザクション処理中に永続化や更新などのエンティティに加えられた
	 * 変更を明示的にデータベースに書きだす操作（flush）の動作確認テスト。
	 * 
	 * ・Customer02エンティティを生成後、Address02エンティティを生成し、
	 * 　Customer02エンティティのaddressフィールドにセットする。
	 * ・先にAddress02エンティティを永続化する。
	 * ・em.flush()を実行し、Address02エンティティに対応するレコードを
	 * 　データベースに先に書きだす。
	 * ・Customer02エンティティを永続化する。
	 * ・コミットする。
	 * 
	 * 通常、トランザクション内でEntityManagerに対して行われたすべての
	 * エンティティに対する更新は、tx.commit()を実行したタイミングで
	 * 一括してデータベースに書きだされる（＝SQL文が発行される）
	 * 
	 * このem.flush()を実行することで、トランザクションコミット前の
	 * タイミングで前もってエンティティに対する更新をデータベースに
	 * 書き出すことができる。（ただし、READ UNCOMMITTED以外のトランザクション
	 * 分離レベルである場合は、データベースに書き出すといっても
	 * コミットするまでは他のトランザクションからその更新が見えない
	 * ことに注意）
	 * 
	 */
	@Test
	public void testSynchronizeWithDatabase() throws Exception {
		
		///// 準備 /////
		
        Customer02 customer = new Customer02("Antony", "Balla", "tballa@mail.com");
        Address02 address = new Address02("Ritherdon Rd", "London", "8QE", "UK");
        customer.setAddress(address);	// addressをcustomerにセット
		
		///// テスト /////
		
        EntityTransaction tx = em.getTransaction();
        
        tx.begin();
        
        em.persist(address);	// Address02エンティティを永続化
        
        em.flush();				// データベースを更新
        						// PERSIST（永続化）操作に対するカスケードが設定
        						// されていないため、Address02エンティティのみに
        						// 対してINSERT文が1回発行される
        
        em.persist(customer);	// 次にCustomer02エンティティを永続化
        
        tx.commit();			// トランザクションをコミット
        						// 残りのCustomer02エンティティのみに対して
        						// INSERT文が1回発行される

        em.clear();
        
		///// 検証 /////
		
        // データベースからidをキーにCustomer02エンティティを取得
        Customer02 persistedCustomer = em.find(Customer02.class, customer.getId());
        // データベースからidをキーにAddress02エンティティを取得
        Address02 persistedAddress = em.find(Address02.class, address.getId());

        // IDの付番によってCustomer02エンティティが永続化されたことを確認
        assertThat(persistedCustomer, is(notNullValue()));
        assertThat(persistedCustomer.getId(), is(customer.getId()));

        // IDの付番によってAddress02エンティティが永続化されたことを確認
        assertThat(persistedAddress, is(notNullValue()));
        assertThat(persistedAddress.getId(), is(address.getId()));
	}
	
	/**
	 * EntityManagerによって管理されるエンティティのデータを
	 * データベースで永続化されているデータで更新するEntityManager.refresh()の
	 * テスト。
	 * 
	 * 直近のflush()もしくはcommit()以降にエンティティに加えられた変更が
	 * すべて無効になる（つまり、直近のflush()もしくはcommit()時にデータベース
	 * 上に更新された値で再度上書きされる）。
	 */
	@Test
	public void testRefreshEntity() throws Exception {
		
		///// 準備 /////
		
        Customer02 customer = new Customer02("Antony", "Balla", "tballa@mail.com");
        Address02 address = new Address02("Ritherdon Rd", "London", "8QE", "UK");
        customer.setAddress(address);	// addressをcustomerにセット
        
        EntityTransaction tx = em.getTransaction();
        
        tx.begin();
        em.persist(address);
        em.persist(customer);
        tx.commit();
        
        em.clear();
        
        // データベースからidをキーにCustomer02エンティティを取得
        Customer02 persistedCustomer = em.find(Customer02.class, customer.getId());
        
		///// テスト＆検証 /////
		
        // まず、先のトランザクションでCustomer02エンティティのfirstName
        // フィールドが「Antony」にセットされ永続化されることを確認
        assertThat(persistedCustomer.getFirstName(), is("Antony"));

        // トランザクション外でCustomer02エンティティのフィールド値を変更する
        // （トランザクション外でsetterによりフィールド値を変更しても
        // データベースは更新されない）
        persistedCustomer.setFirstName("New First Name");
        assertThat(persistedCustomer.getFirstName(), is("New First Name"));
        
        // EntityManagerによって管理されるエンティティのフィールド値を
        // データベースで永続化されているフィールド値で更新
        em.refresh(persistedCustomer);
        
        // Customer02エンティティのfirstNameフィールドの値は"Antony"に戻っている
        assertThat(persistedCustomer.getFirstName(), is("Antony"));
	}
	
	/**
	 * エンティティがEntityManagerの管理対象かどうかを取得する
	 * EntityManager.contains()のテスト。
	 * 
	 * このテストでは、永続化直後のエンティティはEntityManagerによる
	 * 管理対象となっていることを示している。
	 * 
	 */
	@Test
	public void testCheckIfEntityContextContainsEntityWhenEntityRegisteredInDBAndExistsInContext() throws Exception {
		
		///// 準備 /////
		
        Customer02 customer = new Customer02("Antony", "Balla", "tballa@mail.com");
        Address02 address = new Address02("Ritherdon Rd", "London", "8QE", "UK");
        customer.setAddress(address);	// addressをcustomerにセット
        
        EntityTransaction tx = em.getTransaction();
        
        tx.begin();
        em.persist(address);
        em.persist(customer);
        tx.commit();
        
		///// テスト＆検証 /////
		
        // 永続化したエンティティはEntityManagerの管理対象となっていることを
        // 確認する
        assertThat(em.contains(customer), is(true));
        assertThat(em.contains(address), is(true));
	}

	/**
	 * エンティティがEntityManagerの管理対象かどうかを取得する
	 * EntityManager.contains()のテスト。
	 * 
	 * このテストでは、EntityManager.remove()によって
	 * データベースから削除されたエンティティはEntityManager
	 * による管理対象外となっていることを示している。
	 */
	@Test
	public void testCheckIfEntityContextContainsEntityWhenEntityRemovedFromDB() throws Exception {
		
		///// 準備 /////
		
        Customer02 customer = new Customer02("Antony", "Balla", "tballa@mail.com");
        Address02 address = new Address02("Ritherdon Rd", "London", "8QE", "UK");
        customer.setAddress(address);	// addressをcustomerにセット
        
        EntityTransaction tx = em.getTransaction();
        
        tx.begin();
        em.persist(address);
        em.persist(customer);
        tx.commit();

        tx.begin();
        em.remove(customer);	// Customer02エンティティを削除
        em.remove(address);		// Address02エンティティを削除
        tx.commit();
        
		///// テスト＆検証 /////
		
        // 削除したエンティティはEntityManagerの管理対象外となっている
        // ことを確認する
        assertThat(em.contains(customer), is(false));
        assertThat(em.contains(address), is(false));
	}
	
	/**
	 * エンティティがEntityManagerの管理対象かどうかを取得する
	 * EntityManager.contains()のテスト。
	 * 
	 * このテストでは、EntityManager.clear()を実行し、永続化に
	 * よって管理対象となっているエンティティをすべて管理対象外と
	 * してからEntityManager.contains()を実行している。
	 * 
	 * EntityManager.clear()を実行しているため、EntityManager.
	 * contains()の実行結果はfalseとなる。
	 * 
	 */
	@Test
	public void testCheckIfEntityContextContainsEntityWhenEntityRegisteredInDBButNotExistInContext() throws Exception {
		
		///// 準備 /////
		
        Customer02 customer = new Customer02("Antony", "Balla", "tballa@mail.com");
        Address02 address = new Address02("Ritherdon Rd", "London", "8QE", "UK");
        customer.setAddress(address);	// addressをcustomerにセット
        
        EntityTransaction tx = em.getTransaction();
        
        tx.begin();
        em.persist(address);
        em.persist(customer);
        tx.commit();
        
        em.clear();		// 全てのエンティティをエンティティマネージャの管理対象外にする

		///// テスト＆検証 /////
		
        // EntityManager.clear()によってEntityManagerの管理対象外となった
        // すべてのエンティティに対してEntityManger.contains()を実行すると
        // falseが返ることを確認する
        assertThat(em.contains(customer), is(false));
        assertThat(em.contains(address), is(false));
	}

	/**
	 * EntityManagerによって管理対象となっているすべての
	 * エンティティを管理対象外にするEntityManager.clear()のテスト。
	 * 
	 * このテストでは、2つのCustomer02エンティティがEntityManager
	 * によって管理されている状態でEntityManager.clear()を実行する。
	 * 
	 * これにより、Customer02エンティティおよびこれらが参照する
	 * Address02エンティティがすべて管理対象外になることを示している。
	 * 
	 */
	@Test
	public void testClearPersistenceContext() throws Exception {
		
		///// 準備 /////
		
        Customer02 customer1 = new Customer02("Antony", "Balla", "tballa@mail.com");
        Address02 address1 = new Address02("Ritherdon Rd", "London", "8QE", "UK");
        customer1.setAddress(address1);	// address1をcustomer1にセット
        
        Customer02 customer2 = new Customer02("Robert", "Howard", "rhoward@mail.com");
        Address02 address2 = new Address02("Chestnut Rd", "London", "8QE", "UK");
        customer2.setAddress(address2);	// address2をcustomer2にセット
        
        EntityTransaction tx = em.getTransaction();
        
        tx.begin();
        em.persist(address1);
        em.persist(customer1);

        em.persist(address2);
        em.persist(customer2);
        tx.commit();
		
        em.clear();		// 全てのエンティティをエンティティマネージャの管理対象外にする
        
		///// テスト＆検証 /////
		
        // すべてのエンティティが管理対象外となることを確認
        assertThat(em.contains(customer1),	is(false));
        assertThat(em.contains(address1), 	is(false));
        assertThat(em.contains(customer2), 	is(false));
        assertThat(em.contains(address2), 	is(false));
	}
	
	/**
	 * 
	 * EntityManagerによって管理対象となっている特定のエンティティを
	 * 管理対象外とするEntityManager.detach()のテスト。
	 * 
	 * このテストでは、2つのCustomer02エンティティがEntityManager
	 * によって管理されている状態で一方のCustomer02エンティティと
	 * このエンティティに関連づけられるAddress02エンティティのみに
	 * 対してEntityManager.detach()を実行する。
	 * 
	 * これにより、1組のCustomer02エンティティ、Address02エンティティ
	 * のみが管理対象外になることを示している。
	 * 
	 */
	@Test
	public void testDetatchEntityFromPersistenceContext() throws Exception {
		
		///// 準備 /////
		
        Customer02 customer1 = new Customer02("Antony", "Balla", "tballa@mail.com");
        Address02 address1 = new Address02("Ritherdon Rd", "London", "8QE", "UK");
        customer1.setAddress(address1);	// address1をcustomer1にセット
        
        Customer02 customer2 = new Customer02("Robert", "Howard", "rhoward@mail.com");
        Address02 address2 = new Address02("Chestnut Rd", "London", "8QE", "UK");
        customer2.setAddress(address2);	// address2をcustomer2にセット
        
        EntityTransaction tx = em.getTransaction();
        
        tx.begin();
        em.persist(address1);
        em.persist(customer1);

        em.persist(address2);
        em.persist(customer2);
        tx.commit();
		
        em.detach(customer1);	// customer1を管理対象外とする
        em.detach(address1);	// address1を管理対象外とする
        						// リレーションにCascadeType.DETACHが
        						// 指定されていないため、リレーション
        						// により関連付けられているエンティティを
        						// まとめて管理対象外とする場合は
        						// 個別にそのエンティティをdetachする
        						// 必要がある
        
		///// テスト＆検証 /////
		
        // customer1, address1は管理対象外となることを確認
        assertThat(em.contains(customer1), is(false));
        assertThat(em.contains(address1), is(false));
        
        // customer2, address2は管理対象のままであることを確認
        assertThat(em.contains(customer2), is(true));
        assertThat(em.contains(address2), is(true));
	}
	
	/**
	 * EntityManagerにより管理対象となっていないエンティティのフィールド値
	 * でデータベースのレコードを更新するEntityManager.merge()のテスト。
	 * 
	 * メソッド名は「merge」となっているが、merge()したエンティティは
	 * 引き続きEntityManagerの管理対象外となっていることに注意が必要。
	 * 
	 */
	@Test
	public void testMergeEntityToPersistenceContext() throws Exception {
		
		///// 準備 /////
		
        Customer02 customer = new Customer02("Antony", "Balla", "tballa@mail.com");
        Address02 address = new Address02("Ritherdon Rd", "London", "8QE", "UK");
        customer.setAddress(address);	// addressをcustomerにセット
        
        EntityTransaction tx = em.getTransaction();
        
        tx.begin();
        em.persist(address);
        em.persist(customer);
        tx.commit();
        
        em.clear();		// 全てのエンティティをエンティティマネージャの管理対象外にする
		
		///// テスト /////
		
        // 管理対象外となったエンティティのフィールド値を変更
        // （この変更はデータベースには反映されない）
        customer.setFirstName("William");
        customer.setLastName("Blumentals");
        
        tx.begin();
        em.merge(customer);       	// エンティティの変更をデータベースへ
        							// 反映する（firstName,lastNameの反映）
        tx.commit();

		///// 検証 /////
		
        // 引き続きエンティティは管理対象外であることを確認
        assertThat(em.contains(customer), is(false));
        
        // データベースからidをキーにCustomer02エンティティを取得
        Customer02 persistedCustomer = em.find(Customer02.class, customer.getId());
        // EntityManager.merge()によるフィールド値の変更が反映されていることを確認
        assertThat(persistedCustomer.getFirstName(), is("William"));
        assertThat(persistedCustomer.getLastName(), is("Blumentals"));
	}
	
	/**
	 * テストメソッドtestMergeEntityToPersistenceContext()における
	 * EntityManager.merge()を使ったデータ変更と同様の処理を
	 * エンティティに対するsetterメソッドの呼び出しで実装した例。
	 * 
	 */
	@Test
	public void testUpdateEntity() throws Exception {
		
		///// 準備 /////
		
        Customer02 customer = new Customer02 ("Antony", "Balla", "tballa@mail.com");
        Address02 address = new Address02("Ritherdon Rd", "London", "8QE", "UK");
        customer.setAddress(address);	// addressをcustomerにセット
        
        EntityTransaction tx = em.getTransaction();
        
        tx.begin();
        em.persist(address);
        em.persist(customer);
        tx.commit();
        
        //em.clear()は行わない（管理対象のまま）
		
		///// テスト /////
        tx.begin();
        customer.setFirstName("William");	// エンティティに変更を加える
        customer.setLastName("Blumentals");
        tx.commit();

		///// 検証 /////
		
        em.clear();
        
        // データベースからidをキーにCustomer02エンティティを取得
        Customer02 persistedCustomer = em.find(Customer02.class, customer.getId());
        // setterによるフィールド値の変更が反映されていることを確認
        assertThat(persistedCustomer.getFirstName(), is("William"));
        assertThat(persistedCustomer.getLastName(), is("Blumentals"));
	}
	
}
