package org.beginningee6.book.chapter04.ex06;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.Persistence;

import org.beginningee6.book.chapter04.ex06.Book06;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * ＠Versionアノテーションを使用したバージョン管理の仕組みを利用した場合での
 * 様々なロックモードを指定した排他処理の例
 * 
 */
public class ConcurrencyTest {
	
	private static final Logger logger = Logger.getLogger(ConcurrencyTest.class
			.getName());

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
		em.createQuery("DELETE FROM Book06").executeUpdate();

		tx.commit();
	}

	/**
	 * バージョン管理のテスト。
	 * 
	 * Book06エンティティで＠Versionアノテーションにより、
	 * バージョニング機能を付与されたversionフィールド
	 * の値がどう変化するかを確認する。
	 */
	@Test
	public void testVersionIncrement() throws Exception {
		
		///// 準備 /////
		
		Book06 book = new Book06(
				"The Hitchhiker's Guide to the Galaxy",
				12.5F,
				"The Hitchhiker's Guide to the Galaxy is a science fiction comedy series created by Douglas Adams.",
				"1-84023-742-2", 354, false);
		
		///// テスト /////
        
		EntityTransaction tx = em.getTransaction();
		
		// バージョン番号を取得（エンティティが最初に永続化される前はnull）
		Integer versionNull = book.getVersion();

		tx.begin();
		em.persist(book);	// 永続化されるタイミングでバージョン番号は０になる
		tx.commit();
		
		// バージョン番号を取得
		Integer version0 = book.getVersion();
        
		// エンティティに変更を加える
		tx.begin();
		book.setNbOfPage(999);
		tx.commit();		// コミットのタイミングでバージョンが１に上がる
        
		// バージョン番号を取得
		Integer version1 = book.getVersion();
		
		///// 検証 /////
		
		assertThat(versionNull, is(nullValue()));
		assertThat(version0, is(0));
		assertThat(version1, is(1));
	}

	/**
	 * 
	 * ロックモードにOPTIMISTIC_FORCE_INCREMENTを指定して
	 * EntityManager.lock()メソッドでエンティティを楽観的
	 * ロックによりロックしてエンティティを更新するテスト。
	 * 
	 * このテストではロック後に、raisePriceByTwoDollars()で
	 * エンティティに変更を加えているため、ロックと
	 * エンティティ更新によりバージョン番号がコミット時に
	 * ２増加するようになっている。
	 * 
	 * 仮に、raisePriceByTwoDollars()によりエンティティを
	 * 更新しない場合は、ロックのみによりバージョン番号が
	 * コミット時に１増加する。
	 * 
	 */
	@Test
	public void testReadThenLockWith_OPTIMISTIC_FORCE_INCREMENT() throws Exception {
		
		///// 準備 /////
		
		Book06 book = new Book06(
				"The Hitchhiker's Guide to the Galaxy",
				12.5F,
				"The Hitchhiker's Guide to the Galaxy is a science fiction comedy series created by Douglas Adams.",
				"1-84023-742-2", 354, false);

		///// テスト /////
        
		EntityTransaction tx = em.getTransaction();

		tx.begin();
		em.persist(book);
		tx.commit();
        
		///// 検証 /////
				
		assertThat(book.getId(), is(notNullValue()));
		// 永続化直後のバージョンは０
		assertThat(book.getVersion(), is(0));
		
		///// テスト /////
        
		tx.begin();

		book = em.find(Book06.class, book.getId());
		
		// 楽観的ロックによりエンティティをロック
		// OPTIMISTIC_FORCE_INCREMENTを使用しているため、
		// ロック時にバージョン番号が＋1強制的にインクリメントされる
		em.lock(book, LockModeType.OPTIMISTIC_FORCE_INCREMENT);

		// エンティティに変更を加える
		// エンティティに何らかの変更が加わった場合は
		// バージョン番号がさらに＋1インクリメントされる
		// （エンティティに変更を加えずにコミットした場合は
		// 　バージョン番号はインクリメントされない）
		book.raisePriceByTwoDollars();

		tx.commit();	// コミット時にインクリメントされたバージョン番号が
						// データベースに反映される。
						// 結果として、ロックとエンティティ変更により、
						// トランザクション開始前のバージョン番号に＋2した
						// 値がデータベースに反映される
        
		///// 検証 /////
		
		// バージョンは０⇒２へ変化
		assertThat(book.getVersion(), is(2));
		assertThat(book.getPrice(), is(14.5f));
	}
	
	/**
	 * 
	 * ロックモードにOPTIMISTIC_FORCE_INCREMENTを指定して
	 * EntityManager.find()メソッドでエンティティをデータベースから
	 * 取得し、さらに、そのエンティティを楽観的ロックによりロックしてから
	 * そのエンティティを更新するテスト。
	 * 
	 * このテストではロック後に、raisePriceByTwoDollars()で
	 * エンティティに変更を加えているため、ロックと
	 * エンティティ更新によりバージョン番号がコミット時に
	 * ２増加するようになっている。
	 * 
	 * 仮に、raisePriceByTwoDollars()によりエンティティを
	 * 更新しない場合は、ロックのみによりバージョン番号が
	 * コミット時に１増加する。
	 * 
	 */
	@Test
	public void testReadAndLockWith_OPTIMISTIC_FORCE_INCREMENT() throws Exception {
		
		///// 準備 /////
		
		Book06 book = new Book06(
				"The Hitchhiker's Guide to the Galaxy",
				12.5F,
				"The Hitchhiker's Guide to the Galaxy is a science fiction comedy series created by Douglas Adams.",
				"1-84023-742-2", 354, false);
		
		///// テスト /////
        
		EntityTransaction tx = em.getTransaction();

		tx.begin();
		em.persist(book);
		tx.commit();
        
		///// 検証 /////
		
		assertThat(book.getId(), is(notNullValue()));
		// 永続化直後のバージョンは０
		assertThat(book.getVersion(), is(0));
		
		///// テスト /////
        
		tx.begin();

		// find()によりエンティティを取得し、同時に楽観的ロックにより
		// エンティティをロックする。
		// OPTIMISTIC_FORCE_INCREMENTを使用しているため、
		// ロック時にバージョン番号が＋1強制的にインクリメントされる
		book = em.find(Book06.class, book.getId(),
				LockModeType.OPTIMISTIC_FORCE_INCREMENT);

		// エンティティに変更を加える
		// エンティティに何らかの変更が加わった場合は
		// バージョン番号がさらに＋1インクリメントされる
		// （エンティティに変更を加えずにコミットした場合は
		// 　バージョン番号はインクリメントされない）
		book.raisePriceByTwoDollars();

		tx.commit();	// コミット時にインクリメントされたバージョン番号が
						// データベースに反映される。
						// 結果として、ロックとエンティティ変更により、
						// トランザクション開始前のバージョン番号に＋2した
						// 値がデータベースに反映される
        
		///// 検証 /////
		
		// バージョンは０⇒２に変化
		assertThat(book.getVersion(), is(2));
		assertThat(book.getPrice(), is(14.5f));
	}

	/**
	 * 
	 * ロックモードにOPTIMISTICを指定してEntityManager.lock()
	 * メソッドでエンティティを楽観的ロックによりロックして
	 * エンティティを更新するテスト。
	 * 
	 * このテストでは、OPTIMISTIC_FORCE_INCREMENTとは異なり、
	 * ロック時にはバージョン番号は+1インクリメントされない。
	 * 
	 * 結果として、raisePriceByTwoDollars()実行による
	 * エンティティ更新によりバージョン番号がコミット時に
	 * １増加するようになっている。
	 * 
	 * 仮に、raisePriceByTwoDollars()によりエンティティを
	 * 更新しない場合は、コミットを行ってもバージョン番号は
	 * 変化しない。
	 * 
	 */
	@Test
	public void testReadThenLockWith_OPTIMISTIC() throws Exception {
		
		///// 準備 /////
		
		Book06 book = new Book06(
				"The Hitchhiker's Guide to the Galaxy",
				12.5F,
				"The Hitchhiker's Guide to the Galaxy is a science fiction comedy series created by Douglas Adams.",
				"1-84023-742-2", 354, false);
		
		///// テスト /////
        
		EntityTransaction tx = em.getTransaction();

		tx.begin();
		em.persist(book);
		tx.commit();
        
		///// 検証 /////
		
		assertThat(book.getId(), is(notNullValue()));
		// 永続化直後のバージョンは０
		assertThat(book.getVersion(), is(0));
		
		///// テスト /////
        
		tx.begin();

		book = em.find(Book06.class, book.getId());
		// 楽観的ロックによりエンティティをロック
		// OPTIMISTICを使用しているため、ロック時には
		// バージョン番号が変化しない
		em.lock(book, LockModeType.OPTIMISTIC);

		// エンティティに変更を加える
		// エンティティに何らかの変更が加わった場合は
		// バージョン番号が＋1インクリメントされる
		// （エンティティに変更を加えずにコミットした場合は
		// 　バージョン番号はインクリメントされない）
		book.raisePriceByTwoDollars();

		tx.commit();	// コミット時にインクリメントされたバージョン番号が
						// データベースに反映される。
						// 結果として、エンティティ変更により、
						// トランザクション開始前のバージョン番号に＋1した
						// 値がデータベースに反映される
        
		///// 検証 /////
		
		// バージョンは０⇒１へ変化
		assertThat(book.getVersion(), is(1));
		assertThat(book.getPrice(), is(14.5f));
	}

	/**
	 * 
	 * ロックモードにOPTIMISTICを指定してEntityManager.find()メソッドで
	 * エンティティをデータベースから取得し、さらに、そのエンティティを
	 * 楽観的ロックによりロックしてからそのエンティティを更新するテスト。
	 * 
	 * このテストでは、OPTIMISTIC_FORCE_INCREMENTとは異なり、
	 * ロック時にはバージョン番号は+1インクリメントされない。
	 * 
	 * 結果として、raisePriceByTwoDollars()実行による
	 * エンティティ更新によりバージョン番号がコミット時に
	 * １増加するようになっている。
	 * 
	 * 仮に、raisePriceByTwoDollars()によりエンティティを
	 * 更新しない場合は、コミットを行ってもバージョン番号は
	 * 変化しない。
	 * 
	 */
	@Test
	public void testReadAndLockWith_OPTIMISTIC() throws Exception {
		
		///// 準備 /////
		
		Book06 book = new Book06(
				"The Hitchhiker's Guide to the Galaxy",
				12.5F,
				"The Hitchhiker's Guide to the Galaxy is a science fiction comedy series created by Douglas Adams.",
				"1-84023-742-2", 354, false);
		
		///// テスト /////
        
		EntityTransaction tx = em.getTransaction();

		tx.begin();
		em.persist(book);
		tx.commit();
        
		///// 検証 /////
		
		assertThat(book.getId(), is(notNullValue()));
		// 永続化直後のバージョンは０
		assertThat(book.getVersion(), is(0));
		
		///// テスト /////
        
		tx.begin();

		// find()によりエンティティを取得し、同時に楽観的
		// ロックによりエンティティをロックする。
		// OPTIMISTICを使用しているため、ロック時には
		// バージョン番号が変化しない
		book = em.find(Book06.class, book.getId(), LockModeType.OPTIMISTIC);

		// エンティティに変更を加える
		// エンティティに何らかの変更が加わった場合は
		// バージョン番号が＋1インクリメントされる
		// （エンティティに変更を加えずにコミットした場合は
		// 　バージョン番号はインクリメントされない）
		book.raisePriceByTwoDollars();

		tx.commit();	// コミット時にインクリメントされたバージョン番号が
						// データベースに反映される。
						// 結果として、エンティティ変更により、
						// トランザクション開始前のバージョン番号に＋1した
						// 値がデータベースに反映される
        
		///// 検証 /////
		
		// バージョンは０⇒１へ変化
		assertThat(book.getVersion(), is(1));
		assertThat(book.getPrice(), is(14.5f));
	}

	/**
	 * 
	 * ロックモードにPESSIMISTIC_FORCE_INCREMENTを指定して
	 * EntityManager.lock()メソッドでエンティティを悲観的ロックに
	 * よりロックしてエンティティを更新するテスト。
	 * 
	 * このテストではロック後に、raisePriceByTwoDollars()で
	 * エンティティに変更を加えているため、ロックと
	 * エンティティ更新によりバージョン番号がコミット時に
	 * ２増加するようになっている。
	 * 
	 * 仮に、raisePriceByTwoDollars()によりエンティティを
	 * 更新しない場合は、ロックのみによりバージョン番号が
	 * コミット時に１増加する。
	 * 
	 * PESSIMISTIC_FORCE_INCREMENTでは、lock()でロックした時に
	 * バージョン番号が＋1インクリメントされるようになっている。
	 * （OPTIMISTIC_FORCE_INCREMENTでは、lock()でロックした時には
	 * バージョン番号のインクリメントは発生せず、コミット時に
	 * まとめてインクリメントが行われる）
	 * 
	 */
	@Test
	public void testReadThenLockWith_PESSIMISTIC_FORCE_INCREMENT() throws Exception {
		
		///// 準備 /////
		
		Book06 book = new Book06(
				"The Hitchhiker's Guide to the Galaxy",
				12.5F,
				"The Hitchhiker's Guide to the Galaxy is a science fiction comedy series created by Douglas Adams.",
				"1-84023-742-2", 354, false);
		
		///// テスト /////
	    
		EntityTransaction tx = em.getTransaction();
	
		tx.begin();
		em.persist(book);
		tx.commit();
	    
		///// 検証 /////
		
		assertThat(book.getId(), is(notNullValue()));
		// 永続化直後のバージョンは０
		assertThat(book.getVersion(), is(0));
		
		///// テスト /////
	    
		tx.begin();
	
		book = em.find(Book06.class, book.getId());
		
		// 悲観ロックによりエンティティをロック
		// PESSIMISTIC_FORCE_INCREMENTを使用しているため、
		// ロック時にバージョン番号が＋1強制的にインクリメントされる
		// また、このインクリメント分を含んだバージョン番号の値を更新する
		// UPDATE文の発行はこの時に実行される。
		em.lock(book, LockModeType.PESSIMISTIC_FORCE_INCREMENT);

		// エンティティに変更を加える
		// エンティティに何らかの変更が加わった場合は
		// バージョン番号がさらに＋1インクリメントされる
		// （エンティティに変更を加えずにコミットした場合は
		// 　バージョン番号はインクリメントされない）
		book.raisePriceByTwoDollars();
	
		tx.commit();	// コミット時にインクリメントされたバージョン番号が
						// データベースに反映される。
						// 結果として、ロックとエンティティ変更により、
						// トランザクション開始前のバージョン番号に＋2した
						// 値がデータベースに反映される
	    
		///// 検証 /////
		
		// バージョンは０⇒２へ変化
		assertThat(book.getVersion(), is(2));
		assertThat(book.getPrice(), is(14.5f));
	}

	/**
	 * 
	 * ロックモードにPESSIMISTIC_FORCE_INCREMENTを指定して
	 * EntityManager.find()メソッドでエンティティをデータベースから
	 * 取得し、さらに、そのエンティティを楽観的ロックによりロックしてから
	 * そのエンティティを更新するテスト。
	 * 
	 * このテストではロック後に、raisePriceByTwoDollars()で
	 * エンティティに変更を加えているため、ロックと
	 * エンティティ更新によりバージョン番号がコミット時に
	 * ２増加するようになっている。
	 * 
	 * 仮に、raisePriceByTwoDollars()によりエンティティを
	 * 更新しない場合は、ロックのみによりバージョン番号が
	 * コミット時に１増加する。
	 * 
	 * PESSIMISTIC_FORCE_INCREMENTでは、find()でロックした時に
	 * バージョン番号が＋1インクリメントされるようになっている。
	 * （OPTIMISTIC_FORCE_INCREMENTでは、find()でロックした時には
	 * バージョン番号のインクリメントは発生せず、コミット時に
	 * まとめてインクリメントが行われる）
	 * 
	 */

	@Test
	public void testReadAndLockWith_PESSIMISTIC_FORCE_INCREMENT() throws Exception {
		
		///// 準備 /////
		
		Book06 book = new Book06(
				"The Hitchhiker's Guide to the Galaxy",
				12.5F,
				"The Hitchhiker's Guide to the Galaxy is a science fiction comedy series created by Douglas Adams.",
				"1-84023-742-2", 354, false);
		
		///// テスト /////
	    
		EntityTransaction tx = em.getTransaction();
	
		tx.begin();
		em.persist(book);
		tx.commit();
	    
		///// 検証 /////
		
		assertThat(book.getId(), is(notNullValue()));
		// 永続化直後のバージョンは０
		assertThat(book.getVersion(), is(0));
		
		///// テスト /////
	    
		tx.begin();
	
		// find()によりエンティティを取得し、同時に悲観的ロックにより
		// エンティティをロックする。
		// PESSIMISTIC_FORCE_INCREMENTを使用しているため、
		// ロック時にバージョン番号が＋1強制的にインクリメントされる
		// また、このインクリメント分を含んだバージョン番号の値を更新する
		// UPDATE文の発行はこの時に実行される。
		book = em.find(Book06.class, book.getId(),
				LockModeType.PESSIMISTIC_FORCE_INCREMENT);

		// エンティティに変更を加える
		// エンティティに何らかの変更が加わった場合は
		// バージョン番号がさらに＋1インクリメントされる
		// （エンティティに変更を加えずにコミットした場合は
		// 　バージョン番号はインクリメントされない）
		book.raisePriceByTwoDollars();
	
		tx.commit();	// コミット時にインクリメントされたバージョン番号が
						// データベースに反映される。
						// 結果として、ロックとエンティティ変更により、
						// トランザクション開始前のバージョン番号に＋2した
						// 値がデータベースに反映される
	    
		///// 検証 /////
		
		// バージョンは０⇒２へ変化
		assertThat(book.getVersion(), is(2));
		assertThat(book.getPrice(), is(14.5f));
	}

	/**
	 * 
	 * ロックモードにPESSIMISTIC_READを指定してEntityManager.lock()
	 * メソッドでエンティティを悲観的ロックによりロックして
	 * エンティティを更新するテスト。
	 * 
	 * このテストでは、PESSIMISTIC_FORCE_INCREMENTとは異なり、
	 * ロック時にはバージョン番号は+1インクリメントされない。
	 * 
	 * 結果として、raisePriceByTwoDollars()実行による
	 * エンティティ更新によりバージョン番号がコミット時に
	 * １増加するようになっている。
	 * 
	 * 仮に、raisePriceByTwoDollars()によりエンティティを
	 * 更新しない場合は、コミットを行ってもバージョン番号は
	 * 変化しない。
	 * 
	 * なお、Apache Derbyの場合、EntityManager.lock()メソッドを
	 * 実行するとロックしたエンティティにマッピングされる
	 * レコードに対して
	 * 
	 * select id from book_ex06 where id=? and version=? for read only with rs
	 * 
	 * というSQL文が実行され、トランザクション内での反復可能読み取り
	 * が保証される。
	 * 
	 */
	@Test
	public void testReadThenLockWith_PESSIMISTIC_READ() throws Exception {
		
		///// 準備 /////
		
		Book06 book = new Book06(
				"The Hitchhiker's Guide to the Galaxy",
				12.5F,
				"The Hitchhiker's Guide to the Galaxy is a science fiction comedy series created by Douglas Adams.",
				"1-84023-742-2", 354, false);
		
		///// テスト /////
        
		EntityTransaction tx = em.getTransaction();

		tx.begin();
		em.persist(book);
		tx.commit();
        
		///// 検証 /////
		
		assertThat(book.getId(), is(notNullValue()));
		// 永続化直後のバージョンは０
		assertThat(book.getVersion(), is(0));
		
		///// テスト /////
        
		tx.begin();

		book = em.find(Book06.class, book.getId());
		// 悲観的ロックによりエンティティをロック
		// PESSIMISTIC_READを使用しているため、ロック時には
		// バージョン番号が変化しない
		// なお、ロックをかけたエンティティについては、
		// トランザクション内での反復可能読み取りが
		// 保証される
		em.lock(book, LockModeType.PESSIMISTIC_READ);

		// エンティティに変更を加える
		// エンティティに何らかの変更が加わった場合は
		// バージョン番号が＋1インクリメントされる
		// （エンティティに変更を加えずにコミットした場合は
		// 　バージョン番号はインクリメントされない）
		book.raisePriceByTwoDollars();

		tx.commit();	// コミット時にインクリメントされたバージョン番号が
						// データベースに反映される。
						// 結果として、エンティティ変更により、
						// トランザクション開始前のバージョン番号に＋1した
						// 値がデータベースに反映される
        
		///// 検証 /////
		
		// バージョンは０⇒１へ変化
		assertThat(book.getVersion(), is(1));
		assertThat(book.getPrice(), is(14.5f));
	}

	/**
	 * 
	 * ロックモードにPESSIMISTIC_READを指定して
	 * EntityManager.find()メソッドでエンティティをデータベースから
	 * 取得し、さらに、そのエンティティを悲観的ロックによりロックしてから
	 * そのエンティティを更新するテスト。
	 * 
	 * このテストでは、PESSIMISTIC_FORCE_INCREMENTとは異なり、
	 * ロック時にはバージョン番号は+1インクリメントされない。
	 * 
	 * 結果として、raisePriceByTwoDollars()実行による
	 * エンティティ更新によりバージョン番号がコミット時に
	 * １増加するようになっている。
	 * 
	 * 仮に、raisePriceByTwoDollars()によりエンティティを
	 * 更新しない場合は、コミットを行ってもバージョン番号は
	 * 変化しない。
	 * 
	 * なお、Apache Derbyの場合、EntityManager.find()メソッドを
	 * 実行するとロックしたエンティティにマッピングされる
	 * レコードに対して
	 * 
	 * select id from book_ex06 where id=? and version=? for read only with rs
	 * 
	 * というSQL文が実行され、トランザクション内での反復可能読み取り
	 * が保証される。
	 * 
	 */
	@Test
	public void testReadAndLockWith_PESSIMISTIC_READ() throws Exception {
		
		///// 準備 /////
		
		Book06 book = new Book06(
				"The Hitchhiker's Guide to the Galaxy",
				12.5F,
				"The Hitchhiker's Guide to the Galaxy is a science fiction comedy series created by Douglas Adams.",
				"1-84023-742-2", 354, false);
		
		///// テスト /////
        
		EntityTransaction tx = em.getTransaction();

		tx.begin();
		em.persist(book);
		tx.commit();
        
		///// 検証 /////
		
		assertThat(book.getId(), is(notNullValue()));
		// 永続化直後のバージョンは０
		assertThat(book.getVersion(), is(0));
		
		///// テスト /////
        
		tx.begin();

		// find()によりエンティティを取得し、同時に悲観的ロックにより
		// エンティティをロックする。
		// PESSIMISTIC_READを使用しているため、ロック時には
		// バージョン番号が変化しない
		// なお、ロックをかけたエンティティについては、
		// トランザクション内での反復可能読み取りが
		// 保証される
		book = em.find(Book06.class, book.getId(), LockModeType.PESSIMISTIC_READ);

		// エンティティに変更を加える
		// エンティティに何らかの変更が加わった場合は
		// バージョン番号が＋1インクリメントされる
		// （エンティティに変更を加えずにコミットした場合は
		// 　バージョン番号はインクリメントされない）
		book.raisePriceByTwoDollars();

		tx.commit();	// コミット時にインクリメントされたバージョン番号が
						// データベースに反映される。
						// 結果として、エンティティ変更により、
						// トランザクション開始前のバージョン番号に＋1した
						// 値がデータベースに反映される
        
		///// 検証 /////
		
		// バージョンは０⇒１へ変化
		assertThat(book.getVersion(), is(1));
		assertThat(book.getPrice(), is(14.5f));
	}

	/**
	 * 
	 * ロックモードにPESSIMISTIC_WRITEを指定してEntityManager.lock()
	 * メソッドでエンティティを悲観的ロックによりロックして
	 * エンティティを更新するテスト。
	 * 
	 * このテストでは、PESSIMISTIC_FORCE_INCREMENTとは異なり、
	 * ロック時にはバージョン番号は+1インクリメントされない。
	 * 
	 * 結果として、raisePriceByTwoDollars()実行による
	 * エンティティ更新によりバージョン番号がコミット時に
	 * １増加するようになっている。
	 * 
	 * 仮に、raisePriceByTwoDollars()によりエンティティを
	 * 更新しない場合は、コミットを行ってもバージョン番号は
	 * 変化しない。
	 * 
	 * なお、Apache Derbyの場合、EntityManager.lock()メソッドを
	 * 実行するとロックしたエンティティにマッピングされる
	 * レコードに対して
	 * 
	 * select id from book_ex06 where id=? and version=? for update with rs
	 * 
	 * というSQL文が実行され、そのレコードに対して書き込みロックがかけられ、
	 * トランザクション処理中での他のトランザクションによるデータの更新を
	 * 防ぐことができる。
	 * 
	 */
	@Test
	public void testReadThenLockWith_PESSIMISTIC_WRITE() throws Exception {
		
		///// 準備 /////
		
		Book06 book = new Book06(
				"The Hitchhiker's Guide to the Galaxy",
				12.5F,
				"The Hitchhiker's Guide to the Galaxy is a science fiction comedy series created by Douglas Adams.",
				"1-84023-742-2", 354, false);
		
		///// テスト /////
        
		EntityTransaction tx = em.getTransaction();

		tx.begin();
		em.persist(book);
		tx.commit();
        
		///// 検証 /////
		
		assertThat(book.getId(), is(notNullValue()));
		// 永続化直後のバージョンは０
		assertThat(book.getVersion(), is(0));
		
		///// テスト /////
        
		tx.begin();

		book = em.find(Book06.class, book.getId());

		// 悲観的ロックによりエンティティをロック
		// PESSIMISTIC_WRITEを使用しているため、ロック時には
		// バージョン番号が変化しない
		// なお、エンティティについては、対応するレコードに
		// 対して書き込みロックがかけられる
		em.lock(book, LockModeType.PESSIMISTIC_WRITE);

		// エンティティに変更を加える
		// エンティティに何らかの変更が加わった場合は
		// バージョン番号が＋1インクリメントされる
		// （エンティティに変更を加えずにコミットした場合は
		// 　バージョン番号はインクリメントされない）
		book.raisePriceByTwoDollars();

		tx.commit();	// コミット時にインクリメントされたバージョン番号が
						// データベースに反映される。
						// 結果として、エンティティ変更により、
						// トランザクション開始前のバージョン番号に＋1した
						// 値がデータベースに反映される
        
		///// 検証 /////
		
		// バージョンは０⇒１へ変化
		assertThat(book.getVersion(), is(1));
		assertThat(book.getPrice(), is(14.5f));
	}

	/**
	 * 
	 * ロックモードにPESSIMISTIC_WRITEを指定して
	 * EntityManager.find()メソッドでエンティティをデータベースから
	 * 取得し、さらに、そのエンティティを悲観的ロックによりロックしてから
	 * そのエンティティを更新するテスト。
	 * 
	 * このテストでは、PESSIMISTIC_FORCE_INCREMENTとは異なり、
	 * ロック時にはバージョン番号は+1インクリメントされない。
	 * 
	 * 結果として、raisePriceByTwoDollars()実行による
	 * エンティティ更新によりバージョン番号がコミット時に
	 * １増加するようになっている。
	 * 
	 * 仮に、raisePriceByTwoDollars()によりエンティティを
	 * 更新しない場合は、コミットを行ってもバージョン番号は
	 * 変化しない。
	 * 
	 * なお、Apache Derbyの場合、EntityManager.find()メソッドを
	 * 実行するとロックしたエンティティにマッピングされる
	 * レコードに対して
	 * 
	 * select id from book_ex06 where id=? and version=? for read only with rs
	 * 
	 * というSQL文が実行され、そのレコードに対して書き込みロックがかけられ、
	 * トランザクション処理中での他のトランザクションによるデータの更新を
	 * 防ぐことができる。
	 * 
	 */
	@Test
	public void testReadAndLockWith_PESSIMISTIC_WRITE() throws Exception {
		
		///// 準備 /////
		
		Book06 book = new Book06(
				"The Hitchhiker's Guide to the Galaxy",
				12.5F,
				"The Hitchhiker's Guide to the Galaxy is a science fiction comedy series created by Douglas Adams.",
				"1-84023-742-2", 354, false);
		
		///// テスト /////
        
		EntityTransaction tx = em.getTransaction();

		tx.begin();
		em.persist(book);
		tx.commit();
        
		///// 検証 /////
		
		assertThat(book.getId(), is(notNullValue()));
		// 永続化直後のバージョンは０
		assertThat(book.getVersion(), is(0));
		
		///// テスト /////
        
		tx.begin();

		// find()によりエンティティを取得し、同時に悲観的ロックにより
		// エンティティをロックする。
		// PESSIMISTIC_WRITEを使用しているため、ロック時には
		// バージョン番号が変化しない
		// なお、エンティティについては、対応するレコードに
		// 対して書き込みロックがかけられる
		book = em.find(Book06.class, book.getId(), LockModeType.PESSIMISTIC_WRITE);

		// エンティティに変更を加える
		book.raisePriceByTwoDollars();

		tx.commit();	// コミット時にインクリメントされたバージョン番号が
						// データベースに反映される。
						// 結果として、エンティティ変更により、
						// トランザクション開始前のバージョン番号に＋1した
						// 値がデータベースに反映される
        
		///// 検証 /////
		
		// バージョンは０⇒１へ変化
		assertThat(book.getVersion(), is(1));
		assertThat(book.getPrice(), is(14.5f));
	}

	/**
	 * 
	 * ロックモードにNONEを指定してEntityManager.lock()
	 * メソッドを実行するが、実質ロックせずにエンティティを
	 * 更新するテスト。
	 * 
	 * このテストでは、ロック時にはバージョン番号は+1
	 * インクリメントされない。
	 * 
	 * 結果として、raisePriceByTwoDollars()実行による
	 * エンティティ更新によりバージョン番号がコミット時に
	 * １増加するようになっている。
	 * 
	 * 仮に、raisePriceByTwoDollars()によりエンティティを
	 * 更新しない場合は、コミットを行ってもバージョン番号は
	 * 変化しない。
	 * 
	 */
	@Test
	public void testReadThenLockWith_NONE() throws Exception {
		
		///// 準備 /////
		
		Book06 book = new Book06(
				"The Hitchhiker's Guide to the Galaxy",
				12.5F,
				"The Hitchhiker's Guide to the Galaxy is a science fiction comedy series created by Douglas Adams.",
				"1-84023-742-2", 354, false);
		
		///// テスト /////
        
		EntityTransaction tx = em.getTransaction();

		tx.begin();
		em.persist(book);
		tx.commit();
        
		///// 検証 /////
		
		assertThat(book.getId(), is(notNullValue()));
		// 永続化直後のバージョンは０
		assertThat(book.getVersion(), is(0));
		
		///// テスト /////
        
		tx.begin();

		book = em.find(Book06.class, book.getId());
		// lock()を実行するが、実質エンティティをロックしないことと等価
		em.lock(book, LockModeType.NONE);

		// エンティティに変更を加える
		// エンティティに何らかの変更が加わった場合は
		// バージョン番号が＋1インクリメントされる
		// （エンティティに変更を加えずにコミットした場合は
		// 　バージョン番号はインクリメントされない）
		book.raisePriceByTwoDollars();

		tx.commit();	// コミット時にインクリメントされたバージョン番号が
						// データベースに反映される。
						// 結果として、エンティティ変更により、
						// トランザクション開始前のバージョン番号に＋1した
						// 値がデータベースに反映される
        
		///// 検証 /////
		
		// バージョンは０⇒１に変化
		assertThat(book.getVersion(), is(1));
		assertThat(book.getPrice(), is(14.5f));
	}

	/**
	 * 
	 * ロックモードにNONEを指定してEntityManager.find()
	 * メソッドを実行するが、エンティティをデータベースから
	 * 取得し、そのエンティティをロックしないでエンティティを
	 * 更新するテスト。
	 * 
	 * このテストでは、ロック時にはバージョン番号は+1
	 * インクリメントされない。
	 * 
	 * 結果として、raisePriceByTwoDollars()実行による
	 * エンティティ更新によりバージョン番号がコミット時に
	 * １増加するようになっている。
	 * 
	 * 仮に、raisePriceByTwoDollars()によりエンティティを
	 * 更新しない場合は、コミットを行ってもバージョン番号は
	 * 変化しない。
	 * 
	 */
	@Test
	public void testReadAndLockWith_NONE() throws Exception {
		
		///// 準備 /////
		
		Book06 book = new Book06(
				"The Hitchhiker's Guide to the Galaxy",
				12.5F,
				"The Hitchhiker's Guide to the Galaxy is a science fiction comedy series created by Douglas Adams.",
				"1-84023-742-2", 354, false);
		
		///// テスト /////
        
		EntityTransaction tx = em.getTransaction();

		tx.begin();
		em.persist(book);
		tx.commit();
        
		///// 検証 /////
		
		assertThat(book.getId(), is(notNullValue()));
		// 永続化直後のバージョンは０
		assertThat(book.getVersion(), is(0));
		
		///// テスト /////
        
		tx.begin();

		// find()によりエンティティを取得するが、そのエンティティには
		// ロックが実質上掛けられていない。
		book = em.find(Book06.class, book.getId(), LockModeType.NONE);

		// エンティティに変更を加える
		// エンティティに何らかの変更が加わった場合は
		// バージョン番号が＋1インクリメントされる
		// （エンティティに変更を加えずにコミットした場合は
		// 　バージョン番号はインクリメントされない）
		book.raisePriceByTwoDollars();

		tx.commit();	// コミット時にインクリメントされたバージョン番号が
						// データベースに反映される。
						// 結果として、エンティティ変更により、
						// トランザクション開始前のバージョン番号に＋1した
						// 値がデータベースに反映される
        
		///// 検証 /////
		
		// バージョンは０⇒１へ変化
		assertThat(book.getVersion(), is(1));
		assertThat(book.getPrice(), is(14.5f));
	}

}
