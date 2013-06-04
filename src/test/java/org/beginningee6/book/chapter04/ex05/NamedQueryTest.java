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
import javax.persistence.TypedQuery;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 * JPQLの名前付きクエリによるエンティティ検索のテスト。
 */
@RunWith(Enclosed.class)
public class NamedQueryTest {
	private static final Logger logger = Logger.getLogger(NamedQueryTest.class.getName());
	
	/**
	 * 各テストメソッドを実行する前の準備として、Customer05エンティティと
	 * Address05エンティティをデータベースから削除する。
	 * 
	 * なお、この準備メソッドは、テストクラスの最初のテストメソッドが実行される前にのみ
	 * 実行される。
	 * 
	 */
	private static void clearData(EntityManager em) throws Exception {
		EntityTransaction tx = em.getTransaction();		
		tx.begin();

		logger.info("Dumping old records...");
		em.createQuery("DELETE FROM Customer05").executeUpdate();
		em.createQuery("DELETE FROM Address05").executeUpdate();

		tx.commit();
	}
	
	/**
	 * 各テストメソッドを実行する前の準備として、Customer05エンティティと
	 * Address05エンティティを生成し、Customer05のaddressフィールドに
	 * Address05エンティティをセットして永続化する。
	 * 
	 * このエンティティの組を６個作成しデータベースに登録しておく。
	 * 
	 * なお、この準備メソッドは、テストクラスの最初のテストメソッドが実行される前にのみ
	 * 実行される。
	 * 
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
	 * 型指定なしの名前付きクエリのテスト。
	 */
	public static class NamedQuery {
		
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
		
		/**
		 * 名前付きクエリを使用して、
		 * Customer05からデータを全て取得する。
		 * 
		 * クエリの定義に関してはCustomer05のクラス定義を参照すること。
		 */
		@Test
		public void testNamedQuery_1() throws Exception {
			
			///// 準備 /////
			
			// "findAll"で定義された名前付きクエリを作成。
			Query query = em.createNamedQuery("findAll");
			
			///// テスト /////
	        
			@SuppressWarnings("unchecked")		// 型が安全でないとの警告を抑える
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////

			assertThat(customers.size(), is(6));
		}
		
		/**
		 * 名前付きクエリを使用して、
		 * Customer05からfirstNameが'Vincent'のデータを全て取得する。
		 * 
		 * クエリの定義に関してはCustomer05のクラス定義を参照すること。
		 */
		@Test
		public void testNamedQuery_2() throws Exception {
			
			///// 準備 /////
			
			// "findVincent"で定義された名前付きクエリを作成。
			Query query = em.createNamedQuery("findVincent");
			
			///// テスト /////
	        
			@SuppressWarnings("unchecked")		// 型が安全でないとの警告を抑える
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(2));
		}
		
		/**
		 * 名前付きクエリを使用して、
		 * Customer05からfirstNameがパラメータで指定された値のデータを全て取得する。
		 * firstNameの検索条件に対するパラメータ値の指定は、クエリ作成後に行う。
		 * 
		 * クエリの定義に関してはCustomer05のクラス定義を参照すること。
		 */
		@Test
		public void testNamedQuery_3() throws Exception {
			
			///// 準備 /////
			
			// "findWithParam"で定義された名前付きクエリを作成。
			Query query = em.createNamedQuery("findWithParam");
			// :fnameパラメータに"Vincent"をセット
			query.setParameter("fname", "Vincent");
			
			///// テスト /////
	        
			@SuppressWarnings("unchecked")		// 型が安全でないとの警告を抑える
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(2));
		}
		
		/**
		 * 名前付きクエリを使用して、
		 * Customer05からデータを全て取得する（定数文字列使用）。
		 * 
		 * クエリの定義に関してはCustomer05のクラス定義を参照すること。
		 */
		@Test
		public void testNamedQuery_4() throws Exception {
			
			///// 準備 /////
			
			// 定数文字列Customer05.FIND_ALLで定義されたクエリを作成。
			Query query = em.createNamedQuery(Customer05.FIND_ALL);
			
			///// テスト /////
	        
			@SuppressWarnings("unchecked")		// 型が安全でないとの警告を抑える
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(6));
		}
		
		/**
		 * 名前付きクエリを使用して、
		 * Customer05からデータを全て取得する（定数文字列使用）。
		 * ただし、取得するデータの最大取得件数を明示的に
		 * 指定し絞り込みを行う。
		 * 
		 * クエリの定義に関してはCustomer05のクラス定義を参照すること。
		 */
		@Test
		public void testNamedQuery_5() throws Exception {
			
			///// 準備 /////
			
			// 定数文字列Customer05.FIND_ALLで定義されたクエリを作成。
			Query query = em.createNamedQuery(Customer05.FIND_ALL);
			// 最大取得件数を最大5件に制限
			query.setMaxResults(5);
			
			///// テスト /////
	        
			@SuppressWarnings("unchecked")		// 型が安全でないとの警告を抑える
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(5));
		}
	}

	/**
	 * 型指定ありの名前付きクエリのテスト。
	 * 
	 * 型が指定されている為、TypedQuery.getResultList()実行時の
	 * ＠@SuppressWarningsアノテーションは不要。
	 * 
	 */
	public static class NamedTypedQuery {
		
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
		
		/**
		 * 型指定ありの名前付きクエリを使用して、
		 * Customer05からデータを全て取得する。
		 * 
		 * クエリの定義に関してはCustomer05のクラス定義を参照すること。
		 * 
		 */
		@Test
		public void testNamedTypedQuery_1() throws Exception {
			
			///// 準備 /////
			
			// "findAll"で定義された型指定クエリを作成。
			TypedQuery<Customer05> query = 
				em.createNamedQuery("findAll", Customer05.class);
			
			///// テスト /////
	        
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(6));
		}
		
		/**
		 * 型指定ありの名前付きクエリを使用して、
		 * Customer05からfirstNameが'Vincent'のデータを全て取得する。
		 * 
		 * クエリの定義に関してはCustomer05のクラス定義を参照すること。
		 */
		@Test
		public void testNamedTypedQuery_2() throws Exception {
			
			///// 準備 /////
			
			// "findVincent"で定義された型指定クエリを作成。
			TypedQuery<Customer05> query = 
				em.createNamedQuery("findVincent", Customer05.class);
			
			///// テスト /////
	        
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(2));
		}
		
		/**
		 * 型指定ありの名前付きクエリを使用して、
		 * Customer05からfirstNameがパラメータで指定された値のデータを全て取得する。
		 * firstNameの検索条件に対するパラメータ値の指定は、クエリ作成後に行う。
		 * 
		 * クエリの定義に関してはCustomer05のクラス定義を参照すること。
		 * 
		 */
		@Test
		public void testNamedTypedQuery_3() throws Exception {
			
			///// 準備 /////
			
			// "findWithParam"で定義された型指定クエリを作成。
			TypedQuery<Customer05> query = 
				em.createNamedQuery("findWithParam", Customer05.class);
			
			// :fnameパラメータに"Vincent"をセット
			query.setParameter("fname", "Vincent");
			
			///// テスト /////
	        
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(2));
		}
		
		/**
		 * 型指定ありの名前付きクエリを使用して、
		 * Customer05からデータを全て取得する（定数文字列使用）。
		 * 
		 * クエリの定義に関してはCustomer05のクラス定義を参照すること。
		 * 
		 */
		@Test
		public void testNamedTypedQuery_4() throws Exception {
			
			///// 準備 /////
			
			// 定数文字列Customer05.FIND_ALLで定義された型指定クエリを作成。
			TypedQuery<Customer05> query = 
				em.createNamedQuery(Customer05.FIND_ALL, Customer05.class);
			
			///// テスト /////
	        
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(6));
		}
		
		/**
		 * 型指定ありの名前付きクエリを使用して、
		 * Customer05からデータを全て取得する（定数文字列使用）。
		 * 
		 * ただし、取得するデータの最大取得件数を明示的に
		 * 指定し絞り込みを行う。
		 * 
		 * クエリの定義に関してはCustomer05のクラス定義を参照すること。
		 * 
		 */
		@Test
		public void testNamedTypedQuery_5() throws Exception {
			
			///// 準備 /////
			
			// 定数文字列Customer05.FIND_ALLで定義された型指定クエリを作成。
			TypedQuery<Customer05> query = 
				em.createNamedQuery(Customer05.FIND_ALL, Customer05.class);
			
			// 最大取得件数を5件に制限
			query.setMaxResults(5);
			
			///// テスト /////
	        
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(5));
		}
	}
}
