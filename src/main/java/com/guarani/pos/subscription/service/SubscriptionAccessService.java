package com.guarani.pos.subscription.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guarani.pos.subscription.dto.PublicPlanResponse;
import com.guarani.pos.subscription.dto.SubscriptionFeatureResponse;
import com.guarani.pos.subscription.model.CompanySubscription;
import com.guarani.pos.subscription.model.SubscriptionPlan;
import com.guarani.pos.subscription.repository.CompanySubscriptionRepository;
import com.guarani.pos.subscription.repository.SubscriptionPlanRepository;

@Service
public class SubscriptionAccessService {

	private final CompanySubscriptionRepository companySubscriptionRepository;
	private final SubscriptionPlanRepository subscriptionPlanRepository;

	public SubscriptionAccessService(CompanySubscriptionRepository companySubscriptionRepository,
			SubscriptionPlanRepository subscriptionPlanRepository) {
		this.companySubscriptionRepository = companySubscriptionRepository;
		this.subscriptionPlanRepository = subscriptionPlanRepository;
	}

	@Transactional(readOnly = true)
	public SubscriptionFeatureResponse getCurrentFeatures(Long companyId) {
		SubscriptionPlan plan = getActivePlan(companyId);

		return new SubscriptionFeatureResponse(plan.getCode(), plan.getName(), plan.getMaxOpenCashSessions(),
				plan.getMaxUsers(), plan.getMaxBranches(), plan.getMaxMonthlyPurchases(), plan.isAllowInternalTicket(), plan.isAllowFiscalPrinter(),
				plan.isAllowElectronicInvoice(), plan.isAllowBancardQr());
	}

	@Transactional(readOnly = true)
	public java.util.List<PublicPlanResponse> getPublicPlans() {
	    return subscriptionPlanRepository.findAllByActiveTrueOrderByPriceMonthlyAsc()
	            .stream()
	            .map(plan -> new PublicPlanResponse(
	                    plan.getCode(),
	                    plan.getName(),
	                    plan.getDescription(),
	                    plan.getPriceMonthly(),
	                    plan.getMaxOpenCashSessions(),
	                    plan.getMaxUsers(),
	                    plan.getMaxBranches(),
	                    plan.getMaxMonthlyPurchases(),
	                    plan.isAllowInternalTicket(),
	                    plan.isAllowFiscalPrinter(),
	                    plan.isAllowElectronicInvoice(),
	                    plan.isAllowBancardQr()
	            ))
	            .toList();
	}
	@Transactional(readOnly = true)
	public CompanySubscription getActiveSubscription(Long companyId) {
		return companySubscriptionRepository.findFirstByCompany_IdAndStatusOrderByStartDateDesc(companyId, "ACTIVE")
				.orElseThrow(() -> new IllegalArgumentException("La empresa no tiene una suscripción activa."));
	}

	public SubscriptionPlan getActivePlan(Long companyId) {
		return companySubscriptionRepository.findByCompany_IdAndStatus(companyId, "ACTIVE")
				.orElseThrow(() -> new IllegalStateException("Empresa sin plan activo")).getPlan();
	}

	@Transactional(readOnly = true)
	public void validateCanOpenCashSession(Long companyId, long currentOpenCashCount) {
		SubscriptionPlan plan = getActivePlan(companyId);
		int maxAllowed = plan.getMaxOpenCashSessions() != null ? plan.getMaxOpenCashSessions() : 1;

		if (currentOpenCashCount >= maxAllowed) {
			throw new IllegalArgumentException("Su plan no permite abrir más cajas.");
		}
	}

	@Transactional(readOnly = true)
	public void validateFiscalPrinterEnabled(Long companyId) {
		SubscriptionPlan plan = getActivePlan(companyId);
		if (!plan.isAllowFiscalPrinter()) {
			throw new IllegalArgumentException("Su plan no incluye impresión fiscal.");
		}
	}

	@Transactional(readOnly = true)
	public void validateElectronicInvoiceEnabled(Long companyId) {
		SubscriptionPlan plan = getActivePlan(companyId);
		if (!plan.isAllowElectronicInvoice()) {
			throw new IllegalArgumentException("Su plan no incluye facturación electrónica.");
		}
	}

	@Transactional(readOnly = true)
	public void validateInternalTicketEnabled(Long companyId) {
		SubscriptionPlan plan = getActivePlan(companyId);
		if (!plan.isAllowInternalTicket()) {
			throw new IllegalArgumentException("Su plan no incluye ticket interno.");
		}
	}

	@Transactional(readOnly = true)
	public void validateCanRegisterPurchase(Long companyId, long currentMonthCount) {
		SubscriptionPlan plan = getActivePlan(companyId);
		Integer maxAllowed = plan.getMaxMonthlyPurchases();

		if (maxAllowed != null && currentMonthCount >= maxAllowed) {
			throw new IllegalArgumentException("Su plan alcanzo el limite mensual de compras registradas.");
		}
	}
}
