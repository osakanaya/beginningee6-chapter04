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
 * 
 * JPQLの動的クエリのテスト。
 * このテストクラスの各テストメソッドでは、様々な方法で指定したSELECT句やWHERE句の
 * 動作確認テストが定義されている。
 * 
 */
@RunWith(Enclosed.class)
public class DynamicQueryTest {
	
	private static final Logger logger = Logger.getLogger(DynamicQueryTest.class.getName());
	
	/**
	 * 各テストメソッドを実行する前の準備として、Customer05/Address05/Book05エンティティ
	 * をデータベースから削除する。
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
		em.createQuery("DELETE FROM Book05").executeUpdate();

		tx.commit();
	}
	
	/**
	 * 各テストメソッドを実行する前の準備として、Customer05エンティティと
	 * Address05エンティティの組を6個、Book05エンティティを3個作成し、
	 * すべてのエンティティのデータをデータベースに登録しておく。
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

        Book05 book01 = new Book05("The Hitchhiker's Guide to the Galaxy", 12F, "The Hitchhiker's Guide to the Galaxy is a science fiction comedy series created by Douglas Adams.", "1-84023-742-2", "Apress", 354, false);
        Book05 book02 = new Book05("Java EE 6", 50F, "Learn about EE 6", "2-84023-742-2", "Apress", 450, true);
        Book05 book03 = new Book05("Narcisse and Golmund", 10F, "One of the best Herman Hesse book", "3-84023-742-2", "Pinguin", 153, false);
        
        EntityTransaction tx = em.getTransaction();		

        tx.begin();
        
        em.persist(customer01);
        em.persist(customer02);
        em.persist(customer03);
        em.persist(customer04);
        em.persist(customer05);
        em.persist(customer06);
        
        em.persist(book01);
        em.persist(book02);
        em.persist(book03);

        tx.commit();
	}
	
	/**
	 * SELECT 句のテスト。
	 */
	public static class SELECT {
		
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
		 * SELECT句の基本的な指定方法。
		 * 
		 * 検索対象としてSELECT句にはエンティティ名（Customer05）を使用。
		 * 
		 * ＠Tableアノテーションを使用している場合、このアノテーションの
		 * name属性の値（データベース上の物理的なテーブル名）はSELECT句に
		 * 指定することはできない。
		 * 
		 */
		@Test
		public void testSELECT_1() throws Exception {
			
			///// 準備 /////
			
			// Customer05の全てのデータを取得するクエリを作成
			Query query = em.createQuery("SELECT c FROM Customer05 c");
			
			///// テスト /////
	        
			// Customer05のリストを取得
			@SuppressWarnings("unchecked")
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(6));
		}
		
		/**
		 * SELECT句の基本的な指定方法。
		 * 
		 * エンティティの特定のフィールドのみを取得する場合は、そのフィールド名を
		 * SELECT句に指定する。（c.firstName）
		 * 
		 * ＠Columnアノテーションを使用している場合、このアノテーションの
		 * name属性の値（データベース上の物理的なカラム名）はSELECT句に指定することは
		 * できない。
		 */
		@Test
		public void testSELECT_2() throws Exception {
			
			///// 準備 /////
			
			// Customer05のfirstName文字列を取得するクエリを作成
			Query query = em.createQuery("SELECT c.firstName FROM Customer05 c");
			
			///// テスト /////
	        
			// firstNameフィールドのリストを取得
			// 検索結果は、firstNameフィールドのJavaデータ型であるStringクラスの
			// Listとして受け取る。
			@SuppressWarnings("unchecked")
			List<String> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(6));
		}
		
		/**
		 * SELECT句にCASE WHEN ... THEN ... ELSE ... END　節を用いて
		 * 検索結果の条件的な演算を行う例。
		 */
		@Test
		public void testSELECT_3() throws Exception {
			
			///// 準備 /////
			
			// Customer05のpriceをeditorが'Apress'の場合は0.5を、
			// そうでなければ0.8を掛けて取得するクエリを作成
			Query query = em.createQuery(
					"SELECT CASE WHEN b.editor = 'Apress' " +
								"THEN (b.price * 0.5) " +
								"ELSE (b.price * 0.8) " +
								"END " +
					"FROM Book05 b " +
					"ORDER BY b.isbn ASC");		// isbnで昇順
			
			///// テスト /////
	        
			// priceフィールドのリストを取得
			// 検索結果は、priceフィールドに相当するデータベースカラムが
			// マッピングされるJavaデータ型であるDoubleクラスのListとして受け取る。
			@SuppressWarnings("unchecked")
			List<Double> prices = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(prices.size(), is(3));
			// editorが'Apress'のため、12(price) * 0.5
			assertThat(prices.get(0), is(Double.valueOf(12 * 0.5)));
			// editorが'Apress'のため、50(price) * 0.5
			assertThat(prices.get(1), is(Double.valueOf(50 * 0.5)));
			// editorが'Apress'でないため、10(price) * 0.8
			assertThat(prices.get(2), is(Double.valueOf(10 * 0.8)));
		}
		
		/**
		 * エンティティのフィールドが他のエンティティを参照するフィールドである時に
		 * その参照されるエンティティを参照するエンティティから取得するSELECT句の例
		 */
		@Test
		public void testSELECT_4() throws Exception {
			
			///// 準備 /////
			
			// Customer05のaddressフィールドを取得するクエリを作成
			// （addressにはAddress05がマッピングされている）
			Query query = em.createQuery("SELECT c.address FROM Customer05 c");
			
			///// テスト /////
	        
			// Address05のリストを取得
			// Customer05のaddressフィールドはAddress05型であるため、
			// Address05クラスのリストとして検索結果を受け取る。
			@SuppressWarnings("unchecked")
			List<Address05> addresses = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(addresses.size(), is(6));
		}
		
		/**
		 * 検索対象のエンティティに他のエンティティを参照するフィールドが
		 * ある場合に、その参照されるエンティティのフィールドを直接SELECT句で
		 * 指定する例。
		 */
		@Test
		public void testSELECT_5() throws Exception {
			
			///// 準備 /////
			
			// Customer05のaddressフィールドによって参照される
			// Address05エンティティのzipcodeフィールドを取得するクエリを作成
			// 「.」記法によりフィールド名を連結して参照を表現することができる。
			Query query = em.createQuery(
					"SELECT c.address.zipcode FROM Customer05 c " +
					"ORDER BY c.address.zipcode");		// address.zipcodeで昇順
			
			///// テスト /////
	        
			// Address05エンティティのzipcode（Stringクラス）のリストを取得
			@SuppressWarnings("unchecked")
			List<String> zipcodes = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(zipcodes.size(), is(6));
			assertThat(zipcodes.get(0), is("345678"));
		}
		
		/**
		 * SELECT句として、「SELECT NEW」構文を使用した場合の例。
		 * 
		 * エンティティのフィールドのうち、一部のフィールドのみを検索結果として
		 * 取得する場合は、以下のような方法で検索処理を実装する必要がある。
		 * 
		 * 1．取得する一部のフィールドをプロパティとして持つPOJOを作成する。
		 * 　 このPOJOは、（1）取得するフィールドの定義、（2）フィールドの
		 * 　 setter/getter、（3）取得するフィールドを引数に取るコンストラクタ
		 * 　 を持つJavaクラスとして作成する。
		 * 2．SELECT NEWを使用して、SELECT句にそのPOJOのクラス名（FQDN名）と
		 * 　 カッコ内に取得するエンティティのフィールドを指定する。
		 * 　 （このフィールドの並び順は、1．で作成するPOJOのコンストラクタの
		 * 　 引数の並び順と一致させる）
		 * 3．Query.getResultList()による検索結果は、1．で作成するPOJOのリストとして
		 * 　 受け取る。
		 * 
		 */
		@Test
		public void testSELECT_6() throws Exception {
			
			///// 準備 /////
			
			// firstName、lastName、address.street1の3つのフィールドを取得するために、
			// 3つの文字列をプロパティとして持つCustomerDTO05オブジェクトの
			// リストを取得するクエリを作成
			Query query = em.createQuery(
					"SELECT NEW org.beginningee6.book.chapter04.ex05.CustomerDTO05(c.firstName, c.lastName, c.address.street1) " +
					"FROM Customer05 c " +
					"ORDER BY c.firstName");		// firstNameで昇順にソートする
			
			///// テスト /////
	        
			// CustomerDTO5オブジェクトのリストとして検索結果を取得
			@SuppressWarnings("unchecked")
			List<CustomerDTO05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(6));
			assertThat(customers.get(0).getFirstName(), is("Antony"));
		}
		
		/**
		 * 
		 * SELECT DISTINCTにより重複値を検索結果から除外する例。
		 * 
		 */
		@Test
		public void testSELECT_7() throws Exception {
			
			///// 準備 /////
			
			// DISTINCT句によりCustomer05からfirstNameの重複値を除外
			// （firstNameの同じ値を持つ検索結果が複数存在しないように）
			// して取得するクエリを作成
			Query query = em.createQuery("SELECT DISTINCT c.firstName FROM Customer05 c");
			
			///// テスト /////
	        
			// 検索結果をfirstNameフィールドのデータ型（String型）のリストとして取得
			@SuppressWarnings("unchecked")
			List<String> customers = query.getResultList();
	        
			///// 検証 /////
			
			// 6件の内、firstNameが'Vincent'のデータが2件あるため、
			// 結果は5件になる
			assertThat(customers.size(), is(5));
		}
		
		/**
		 * 集計関数のテスト。
		 * 
		 * AVG、COUNT、MAX、MIN、SUM 等の集計関数をSELECT句で使用できる。
		 */
		@Test
		public void testSELECT_8() throws Exception {
			
			///// 準備 /////
			
			// Customer05エンティティの件数を取得するクエリを作成
			Query query = em.createQuery("SELECT COUNT(c) FROM Customer05 c");
			
			///// テスト /////
	        
			// getSingleResult()メソッドで単一結果を取得
			Long count = (Long)query.getSingleResult();
	        
			///// 検証 /////
			
			assertThat(count, is(Long.valueOf(6)));
		}
		
	}
	
	/**
	 * WHERE 条件句のテスト。
	 */
	public static class WHERE {
		
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
		 * 
		 * AND条件により複数の条件を同時に満たすエンティティを検索する例
		 * 
		 */
		@Test
		public void testWHERE_1() throws Exception {
			
			///// 準備 /////
			
			// Customer05からfirstNameが'Vincent'かつaddress.countryが'AU'
			// のデータを取得するクエリを作成
			Query query = em.createQuery(
					"SELECT c FROM Customer05 c " +
					"WHERE c.firstName = 'Vincent' AND c.address.country = 'AU'");
			
			///// テスト /////
	        
			@SuppressWarnings("unchecked")
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(1));
			assertThat(customers.get(0).getLastName(), is("Dubosc"));
		}
		
		/**
		 * 
		 * WHERE句に不等号条件を指定する例
		 * 
		 */
		@Test
		public void testWHERE_2() throws Exception {
			
			///// 準備 /////
			
			// Customer05からageが18以下のデータを取得する
			Query query = em.createQuery(
					"SELECT c FROM Customer05 c " +
					"WHERE c.age > 18");
			
			///// テスト /////
	        
			@SuppressWarnings("unchecked")
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(4));
		}
		
		/**
		 * WHERE句にNOT条件及びBETWEEN ... AND ...を指定した例
		 * 
		 * BETWEEN m AND n で m以上n以下、
		 * NOT BETWEEN m AND n で mより小さい または nより大きいとなる。
		 */
		@Test
		public void testWHERE_3() throws Exception {
			
			///// 準備 /////
			
			// Customer05からageが39以下または51以上のデータを取得する
			Query query = em.createQuery(
					"SELECT c FROM Customer05 c " +
					"WHERE c.age NOT BETWEEN 40 AND 50");
			
			///// テスト /////
	        
			@SuppressWarnings("unchecked")
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(4));
		}
		
		/**
		 * 
		 * WHERE ... IN (...) の例。
		 * 
		 * IN句で列挙されたいずれかの値をフィールドの値として持つ
		 * エンティティが検索される。
		 * 
		 */
		@Test
		public void testWHERE_4() throws Exception {
			
			///// 準備 /////
			
			// Customer05からaddress.countryが
			// IN () に含まれる'FR'か'PT'のデータを取得する
			Query query = em.createQuery(
					"SELECT c FROM Customer05 c " +
					"WHERE c.address.country IN ('FR', 'PT')");
			
			///// テスト /////
	        
			@SuppressWarnings("unchecked")
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(2));
		}
		
		/**
		 * LIKEを使用して指定した文字列に部分一致するフィールド値
		 * を持つエンティティを検索する。
		 * 
		 * ％は1文字以上の任意の文字列にマッチする。
		 * 
		 */
		@Test
		public void testWHERE_5() throws Exception {
			
			///// 準備 /////
			
			// Customer05からc.emailの最後が'carmail.com'のデータを取得する
			Query query = em.createQuery(
					"SELECT c FROM Customer05 c " +
					"WHERE c.email LIKE '%carmail.com'");
			
			///// テスト /////
	        
			@SuppressWarnings("unchecked")
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(1));
		}
		
	}
	
	/**
	 * パラメータのバインドを利用したクエリのテスト。
	 */
	public static class ParameterBindings {
		
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
		 * 位置指定パラメータ（? パラメータ）を使用。
		 */
		@Test
		public void testBindingParameters_1() throws Exception {
			
			///// 準備 /////
			
			Query query = em.createQuery(
					"SELECT c FROM Customer05 c " +
					"WHERE c.firstName = ?1 AND c.address.country = ?2");
			query.setParameter(1, "Vincent");	// 	?1 パラメータをセット
			query.setParameter(2, "AU");		// 	?2 パラメータをセット
			
			///// テスト /////
	        
			@SuppressWarnings("unchecked")
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(1));
			assertThat(customers.get(0).getLastName(), is("Dubosc"));
		}
		
		/**
		 * 名前付きパラメータ（: パラメータ）を使用。
		 */
		@Test
		public void testBindingParameters_2() throws Exception {
			
			///// 準備 /////
			
			Query query = em.createQuery(
					"SELECT c FROM Customer05 c " +
					"WHERE c.firstName = :fname AND c.address.country = :country");
			query.setParameter("fname", "Vincent");	// 	:fname パラメータをセット
			query.setParameter("country", "AU");	// 	:country パラメータをセット
			
			///// テスト /////
	        
			@SuppressWarnings("unchecked")
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(1));
			assertThat(customers.get(0).getLastName(), is("Dubosc"));
		}
	}
	
	/**
	 * その他、クエリの様々な指定方法を示した例
	 */
	public static class Miscellaneous {
		
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
		 * サブクエリのテスト。
		 */
		@Test
		public void testSubquery() throws Exception {
			
			///// 準備 /////
			
			// Customer05でageが最小のデータをfirstNameの昇順で取得
			Query query = em.createQuery(
					"SELECT c FROM Customer05 c " +
					"WHERE c.age = (SELECT MIN(cust.age) FROM Customer05 cust) " +
					"ORDER BY c.firstName");	// 指定のない場合は昇順
			
			///// テスト /////
	        
			@SuppressWarnings("unchecked")
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			// ageが14のデータが２件
			assertThat(customers.size(), is(2));
			assertThat(customers.get(0).getAge(), is(14));
			
			// ORDER BYはデフォルトで昇順なので"Antony"、"Vincent"の順
			assertThat(customers.get(0).getFirstName(), is("Antony"));
			assertThat(customers.get(1).getFirstName(), is("Vincent"));
		}
		
		/**
		 * ORDER BY句により検索されるエンティティを降順にソートする例
		 */
		@Test
		public void testORDER_BY_1() throws Exception {
			
			///// 準備 /////
			
			// Customer05でageが18より小さいものをageの降順で取得
			Query query = em.createQuery(
					"SELECT c FROM Customer05 c " +
					"WHERE c.age > 18 " +
					"ORDER BY c.age DESC");
			
			///// テスト /////
	        
			@SuppressWarnings("unchecked")
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(4));
			assertThat(customers.get(0).getFirstName(), is("David"));
			assertThat(customers.get(1).getFirstName(), is("Sebastian"));
			assertThat(customers.get(2).getFirstName(), is("Vincent"));
			assertThat(customers.get(3).getFirstName(), is("Frederic"));
		}
		
		/**
		 * ORDER BY句で複数のフィールドでソートする例
		 */
		@Test
		public void testORDER_BY_2() throws Exception {
			
			///// 準備 /////
			
			// Customer05でageが50より小さいものを
			// ageの昇順で、ageが同じものはその中のaddress.countryの降順で取得
			Query query = em.createQuery(
					"SELECT c FROM Customer05 c " +
					"WHERE c.age < 50 " +
					"ORDER BY c.age ASC, c.address.country DESC");
			
			///// テスト /////
	        
			@SuppressWarnings("unchecked")
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(4));
			// age:14 address.country:"FR"
			assertThat(customers.get(0).getFirstName(), is("Antony"));
			// age:14 address.country:"AU"
			assertThat(customers.get(1).getFirstName(), is("Vincent"));
			// age:41
			assertThat(customers.get(2).getFirstName(), is("Frederic"));
			// age:45
			assertThat(customers.get(3).getFirstName(), is("Vincent"));
		}
		
		/**
		 * 
		 * GROUP BY句により検索されるエンティティをグルーピングする例
		 * 
		 * GROUP BYを使用してaddress.countryとその数をグループ化した
		 * 結果を取得する。
		 * 
		 * GROUP BYの検索結果受け取り用のオブジェクトCustomerCountryCountDTO05
		 * をSELECT NEWで生成する。
		 * コンストラクタの引数はaddress.countryとその数。
		 * 
		 * getResultList()でCustomerCountryCountDTO05オブジェクトの
		 * リストを取得する。
		 * 
		 */
		@Test
		public void testGROUP_BY() throws Exception {
			
			///// 準備 /////
			
			// address.countryでグループ化する（address.countryで昇順）
			Query query = em.createQuery(
					"SELECT NEW org.beginningee6.book.chapter04.ex05.CustomerCountryCountDTO05(c.address.country, COUNT(c)) " +
					"FROM Customer05 c " +
					"GROUP BY c.address.country " +
					"ORDER BY c.address.country ASC");
			
			///// テスト /////
	        
			// CustomerCountryCountDTO05のリストを取得
			@SuppressWarnings("unchecked")
			List<CustomerCountryCountDTO05> counts = query.getResultList();
	        
			///// 検証 /////
			
			// グループ化の結果は５件
			assertThat(counts.size(), is(5));
			
			// "AU"が２
			assertThat(counts.get(0).getCountry(), is("AU"));
			assertThat(counts.get(0).getCount(), is(2L));
			// "BR"が１
			assertThat(counts.get(1).getCountry(), is("BR"));
			assertThat(counts.get(1).getCount(), is(1L));
			// "FR"が１
			assertThat(counts.get(2).getCountry(), is("FR"));
			assertThat(counts.get(2).getCount(), is(1L));
			// "PT"が１
			assertThat(counts.get(3).getCountry(), is("PT"));
			assertThat(counts.get(3).getCount(), is(1L));
			// "UK"が１
			assertThat(counts.get(4).getCountry(), is("UK"));
			assertThat(counts.get(4).getCount(), is(1L));
		}
		
		/**
		 * HAVING句によりグルーピングした結果に条件を適用する例
		 */
		@Test
		public void testHAVING() throws Exception {
			
			///// 準備 /////
			
			// address.countryでグループ化する（address.countryで昇順）
			// address.countryが'PT'のものを含めない
			Query query = em.createQuery(
					"SELECT NEW org.beginningee6.book.chapter04.ex05.CustomerCountryCountDTO05(c.address.country, COUNT(C)) " +
					"FROM Customer05 c " +
					"GROUP BY c.address.country " +
					"HAVING c.address.country <> 'PT' " +
					"ORDER BY c.address.country ASC");
			
			///// テスト /////
	        
			@SuppressWarnings("unchecked")
			List<CustomerCountryCountDTO05> counts = query.getResultList();
	        
			///// 検証 /////
			
			// グループ化の結果は４件
			// address.countryが'PT'のものは含まれていない
			assertThat(counts.size(), is(4));

			assertThat(counts.get(0).getCountry(), is("AU"));
			assertThat(counts.get(0).getCount(), is(2L));
			assertThat(counts.get(1).getCountry(), is("BR"));
			assertThat(counts.get(1).getCount(), is(1L));
			assertThat(counts.get(2).getCountry(), is("FR"));
			assertThat(counts.get(2).getCount(), is(1L));
			assertThat(counts.get(3).getCountry(), is("UK"));
			assertThat(counts.get(3).getCount(), is(1L));
		}
	}
	
	/**
	 * 型指定クエリのテスト。
	 */
	public static class DynamicTypedQuery {
		
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
		 * 
		 * 型指定クエリの基本的な使用方法
		 * 
		 * 作成するクエリは、検索されるエンティティのタイプを
		 * 型パラメータに持つTypedQuery型として作成する。
		 * 
		 * また、EntityManager.createQuery()の第2引数にも
		 * 検索されるエンティティのクラスを指定する。
		 * 
		 */
		@Test
		public void testTypedQuery_1() throws Exception {
			
			///// 準備 /////
			
			// Customer05の全てを検索する型指定クエリを作成
			TypedQuery<Customer05> query = em.createQuery(
					"SELECT c " +
					"FROM Customer05 c", Customer05.class);
			
			///// テスト /////
	        
			// 型指定クエリの実行
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(6));
		}
		
		/**
		 * 
		 * 型指定クエリを使用した検索の例
		 * （WHERE句による条件指定あり）
		 * 
		 */
		@Test
		public void testTypedQuery_2() throws Exception {
			
			///// 準備 /////
			
			// Customer05でfirstNameが'Vincent'のものを検索
			TypedQuery<Customer05> query = em.createQuery(
					"SELECT c " +
					"FROM Customer05 c " +
					"WHERE c.firstName = 'Vincent'", Customer05.class);
			
			///// テスト /////
	        
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(2));
		}
		
		/**
		 * 
		 * 型指定クエリを使用した検索の例
		 * （WHERE句の検索条件指定で位置指定パラメータによるバインドを使用）
		 * 
		 */
		@Test
		public void testTypedQuery_3() throws Exception {
			
			///// 準備 /////
			
			// Customer05でfirstNameとaddress.countryが特定のものを検索
			TypedQuery<Customer05> query = em.createQuery(
					"SELECT c " +
					"FROM Customer05 c " +
					"WHERE c.firstName = ?1 AND c.address.country = ?2",
					Customer05.class);
			query.setParameter(1, "Vincent");	// 	?1 パラメータをセット
			query.setParameter(2, "AU");		// 	?2 パラメータをセット
			
			///// テスト /////
	        
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(1));
			assertThat(customers.get(0).getLastName(), is("Dubosc"));
		}
		
		/**
		 * 
		 * 型指定クエリを使用した検索の例
		 * （WHERE句の検索条件指定で名前付きパラメータによるバインドを使用）
		 * 
		 */
		@Test
		public void testTypedQuery_4() throws Exception {
			
			///// 準備 /////
			
			// Customer05でfirstNameとaddress.countryが特定のものを検索
			TypedQuery<Customer05> query = em.createQuery(
					"SELECT c " +
					"FROM Customer05 c " +
					"WHERE c.firstName = :fname AND c.address.country = :country",
					Customer05.class);
			query.setParameter("fname", "Vincent");	// 	:fname パラメータをセット
			query.setParameter("country", "AU");	// 	:country パラメータをセット
			
			///// テスト /////
	        
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(1));
			assertThat(customers.get(0).getLastName(), is("Dubosc"));
		}
		
		/**
		 * 
		 * 型指定クエリを使用した検索の例
		 * （ORDER BY句によるソートと最大取得件数の指定あり）
		 * 
		 * ※この例では、
		 * 　条件にマッチするエンティティの件数＞最大取得件数
		 * 　となっている。
		 * 
		 */
		@Test
		public void testTypedQuery_5() throws Exception {
			
			///// 準備 /////
			
			TypedQuery<Customer05> query = em.createQuery(
					"SELECT c " +
					"FROM Customer05 c " +
					"ORDER BY c.age ASC",	// ageで昇順指定
					Customer05.class);
			query.setMaxResults(3);			// 先頭最大３件までを取得
			
			///// テスト /////
	        
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			assertThat(customers.size(), is(3));
			assertThat(customers.get(0).getAge(), is(14));
			assertThat(customers.get(1).getAge(), is(14));
			assertThat(customers.get(2).getAge(), is(41));
		}
		
		/**
		 * 
		 * 型指定クエリを使用した検索の例
		 * （ORDER BY句によるソートと最大取得件数の指定あり）
		 * 
		 * ※この例では、
		 * 　条件にマッチするエンティティの件数＜最大取得件数
		 * 　となっている。
		 * 
		 */
		@Test
		public void testTypedQuery_6() throws Exception {
			
			///// 準備 /////
			
			TypedQuery<Customer05> query = em.createQuery(
					"SELECT c " +
					"FROM Customer05 c " +
					"ORDER BY c.age ASC",	// ageで昇順指定
					Customer05.class);
			query.setMaxResults(10);		// 先頭最大10件までを取得
			
			///// テスト /////
	        
			List<Customer05> customers = query.getResultList();
	        
			///// 検証 /////
			
			// 最大取得件数は10件だが６件しかない為、結果は６件
			assertThat(customers.size(), is(6));
			assertThat(customers.get(0).getAge(), is(14));
			assertThat(customers.get(1).getAge(), is(14));
			assertThat(customers.get(2).getAge(), is(41));
		}
	}
}
