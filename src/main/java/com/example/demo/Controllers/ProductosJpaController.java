package com.example.demo.Controllers;

import com.example.demo.Controllers.exceptions.NonexistentEntityException;
import com.example.demo.Models.Productos;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins="/*")
@RestController
@RequestMapping("/productos")
public class ProductosJpaController implements Serializable {

    public ProductosJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    @CrossOrigin("*")
    @PostMapping()
    public HashMap<String, String> create(@RequestBody Productos productos) {
        HashMap<String, String> map = new HashMap<>();
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(productos);
            em.getTransaction().commit();
            map.put("msj", "Ok");
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return map;
    }

    @CrossOrigin("*")
    @PutMapping()
    public HashMap<String, String> edit(@ RequestBody Productos productos) throws NonexistentEntityException, Exception {
        HashMap<String, String> map = new HashMap<>();
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            productos = em.merge(productos);
            em.getTransaction().commit();
            map.put("msg", "Ok") ;
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = productos.getId();
                if (findProducto(id) == null) {
                    throw new NonexistentEntityException("The productos with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return map;
    }
    
    @CrossOrigin("*")
    @DeleteMapping("/{id}")
    public HashMap <String, String> destroy(@PathVariable("id") Integer id) throws NonexistentEntityException {
        HashMap<String, String> map = new HashMap<>();
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Productos productos;
            try {
                productos = em.getReference(Productos.class, id);
                productos.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The productos with id " + id + " no longer exists.", enfe);
            }
            em.remove(productos);
            em.getTransaction().commit();
            map.put("msj", "Ok");
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return map;
    }

    @CrossOrigin(origins="*")
    @GetMapping()
    public List<Productos> findProductosEntities() {
        return findProductosEntities(true, -1, -1);
    }

    public List<Productos> findProductosEntities(int maxResults, int firstResult) {
        return findProductosEntities(false, maxResults, firstResult);
    }

    private List<Productos> findProductosEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Productos.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @CrossOrigin("*")
    @GetMapping("/{id}")
    public Productos findProducto(@PathVariable("id") Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Productos.class, id);
        } finally {
            em.close();
        }
    }

    public int getProductosCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Productos> rt = cq.from(Productos.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
