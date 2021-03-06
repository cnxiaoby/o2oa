package com.x.processplatform.assemble.surface.jaxrs.workcompleted;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.x.base.core.bean.NameValueCountPair;
import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.http.ActionResult;
import com.x.base.core.http.EffectivePerson;
import com.x.processplatform.assemble.surface.Business;
import com.x.processplatform.core.entity.content.WorkCompleted;
import com.x.processplatform.core.entity.content.WorkCompleted_;
import com.x.processplatform.core.entity.element.Application;

class ActionListCountWithProcess extends ActionBase {

	ActionResult<List<NameValueCountPair>> execute(EffectivePerson effectivePerson, String applicationFlag)
			throws Exception {
		try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
			ActionResult<List<NameValueCountPair>> result = new ActionResult<>();
			Business business = new Business(emc);
			List<NameValueCountPair> wraps = new ArrayList<>();
			Application application = business.application().pick(applicationFlag);
			String applicationId = (null != application) ? application.getId() : applicationFlag;
			EntityManager em = business.entityManagerContainer().get(WorkCompleted.class);
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<String> cq = cb.createQuery(String.class);
			Root<WorkCompleted> root = cq.from(WorkCompleted.class);
			Predicate p = cb.equal(root.get(WorkCompleted_.creatorPerson), effectivePerson.getName());
			p = cb.and(p, cb.equal(root.get(WorkCompleted_.application), applicationId));
			cq.select(root.get(WorkCompleted_.process)).where(p).distinct(true);
			for (String str : em.createQuery(cq).getResultList()) {
				NameValueCountPair o = new NameValueCountPair();
				o.setValue(str);
				o.setName(this.getProcessName(business, effectivePerson, str));
				o.setCount(this.countWithProcess(business, effectivePerson, str));
				wraps.add(o);
			}
			result.setData(wraps);
			return result;
		}
	}

	private Long countWithProcess(Business business, EffectivePerson effectivePerson, String id) throws Exception {
		EntityManager em = business.entityManagerContainer().get(WorkCompleted.class);
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<WorkCompleted> root = cq.from(WorkCompleted.class);
		Predicate p = cb.equal(root.get(WorkCompleted_.creatorPerson), effectivePerson.getName());
		p = cb.and(p, cb.equal(root.get(WorkCompleted_.process), id));
		cq.select(cb.count(root)).where(p);
		return em.createQuery(cq).getSingleResult();
	}

}