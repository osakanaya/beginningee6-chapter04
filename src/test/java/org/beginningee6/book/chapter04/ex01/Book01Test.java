package org.beginningee6.book.chapter04.ex01;

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
 * JavaSE環境で動作するアプリケーション環境でのエンティティの
 * 永続化に関するテスト。
 * 
 * これまでArquillianを使用して示したアプリケーションサーバのJavaEE
 * 環境上でのインコンテナテストではなく、アプリケーションサーバを
 * 使用しない、JavaVM上で動作する（main()関数から始まるような）JavaSE
 * アプリケーションからエンティティを操作する手順を示している。
 * 
 * JavaEE環境上のインコンテナテストとは異なり、アプリケーションサーバ
 * などの外部から＠PersistenceContextや＠Injectアノテーションにより
 * EntityManagerやUserTransactionを注入してもらうことはできないため、
 * 以下の手順でEntityManagerの取得とトランザクションの管理を明示的に
 * 行う必要がある。
 * 
 * 1．永続性ユニットの名前を指定して、EntityManagerFactoryを生成する。
 * 2．生成したEntityManagerFactoryからEntityManagerを生成する。
 * 3．EntityManagerからEntityTransactionを取得する。
 * 4. 取得したEntityTransactionのbegin()メソッドを実行してトランザクションを開始する。
 * 5. EntityTransactionのcommit()またはrollback()メソッドを実行してトランザクションを
 * 　 コミットまたはロールバックする。
 * 
 * また、終了時にEntityManager、EntityManagerFactoryの順でクローズ処理を行う必要が
 * ある。
 * 
 */
public class Book01Test {
	
	private static final Logger logger = Logger.getLogger(Book01Test.class.getName());
	
	private static EntityManagerFactory emf;
	private static EntityManager em;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		
		// 永続性ユニットの名前を指定して、EntityManagerFactoryを取得
		emf = Persistence.createEntityManagerFactory("test");
		
		// EntityManagerFactoryからEntityManagerを取得
		em = emf.createEntityManager();
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception {
		
		// EntityManagerのクローズ
		if (em != null) {
			em.close();
		}
		
		// EntityManagerFactoryのクローズ
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
		em.createQuery("DELETE FROM Book01").executeUpdate();
		
		tx.commit();
	}
	
	/**
	 * EntityManagerからEntityTransactionオブジェクトを取得して、
	 * トランザクション処理を行う。
	 */
	@Test
	public void testCreateABook() throws Exception {
		
		///// 永続化するエンティティを準備 /////
		
		Book01 book = new Book01();
        book.setTitle("The Hitchhiker's Guide to the Galaxy");
        book.setPrice(12.5F);
        book.setDescription("Science fiction comedy book");
        book.setIsbn("1-84023-742-2");
        book.setNbOfPage(354);
        book.setIllustrations(false);
		
		///// エンティティを永続化する /////
		
        // EntityManagerからEntityTransactionオブジェクトを取得
        EntityTransaction tx = em.getTransaction();
        // トランザクションを開始
        tx.begin();
        // Bookエンティティを永続化
        em.persist(book);
        // トランザクションをコミット
        tx.commit();
        
		///// エンティティが永続化されたことを検証する /////
        em.clear();
		
        // 主キーが付番されたことを確認
        assertThat(book.getId(), is(notNullValue()));        
        
        // データベースからidをキーに検索してBook01オブジェクトを取得
        Book01 persisted = em.find(Book01.class, book.getId());
        // データのidを検証
        assertThat(persisted.getId(), is(book.getId()));
        // データのtitleを検証
        assertThat(persisted.getTitle(), is("The Hitchhiker's Guide to the Galaxy"));
	}
}
