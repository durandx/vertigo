/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.dynamo.plugins.environment.registries.domain;

import io.vertigo.core.dsl.dynamic.DynamicDefinition;
import io.vertigo.core.dsl.dynamic.DynamicDefinitionRepository;
import io.vertigo.core.dsl.entity.Entity;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.core.spaces.definiton.DefinitionUtil;
import io.vertigo.core.spaces.definiton.KernelGrammar;
import io.vertigo.dynamo.domain.metamodel.ComputedExpression;
import io.vertigo.dynamo.domain.metamodel.ConstraintDefinition;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtDefinitionBuilder;
import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.dynamo.domain.metamodel.DtStereotype;
import io.vertigo.dynamo.domain.metamodel.FormatterDefinition;
import io.vertigo.dynamo.domain.metamodel.Properties;
import io.vertigo.dynamo.domain.metamodel.PropertiesBuilder;
import io.vertigo.dynamo.domain.metamodel.Property;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNode;
import io.vertigo.dynamo.domain.metamodel.association.AssociationSimpleDefinition;
import io.vertigo.dynamo.domain.util.AssociationUtil;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.registries.AbstractDynamicRegistryPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author pchretien
 */
public final class DomainDynamicRegistryPlugin extends AbstractDynamicRegistryPlugin {
	private static final Logger LOGGER = Logger.getLogger(DomainDynamicRegistryPlugin.class);
	private static final String DOMAIN_PREFIX = DefinitionUtil.getPrefix(Domain.class);
	private static final String ASSOCIATION_SIMPLE_DEFINITION_PREFIX = DefinitionUtil.getPrefix(AssociationSimpleDefinition.class);
	private static final String ASSOCIATION_NN_DEFINITION_PREFIX = DefinitionUtil.getPrefix(AssociationNNDefinition.class);
	private static final char SEPARATOR = Definition.SEPARATOR;
	private final Map<String, DtDefinitionBuilder> dtDefinitionBuilders = new HashMap<>();

	/**
	 * Constructeur.
	 */
	public DomainDynamicRegistryPlugin() {
		super(DomainGrammar.GRAMMAR);

	}

	/** {@inheritDoc} */
	@Override
	public List<DynamicDefinition> getRootDynamicDefinitions() {
		final List<DynamicDefinition> dynamicDefinitions = new ArrayList<>();
		//On liste les types primitifs
		final Entity dataTypeEntity = KernelGrammar.getDataTypeEntity();
		for (final DataType type : DataType.values()) {
			dynamicDefinitions.add(DynamicDefinitionRepository.createDynamicDefinitionBuilder(type.name(), dataTypeEntity, null).build());
		}
		return dynamicDefinitions;
	}

	/** {@inheritDoc} */
	@Override
	public Option<Definition> createDefinition(final DefinitionSpace definitionSpace, final DynamicDefinition xdefinition) {
		final Entity entity = xdefinition.getEntity();
		final Definition definition;
		if (entity.equals(DomainGrammar.DOMAIN_ENTITY)) {
			definition = createDomain(definitionSpace, xdefinition);
		} else if (entity.equals(DomainGrammar.DT_DEFINITION_ENTITY)) {
			definition = createDtDefinition(definitionSpace, xdefinition);
		} else if (entity.equals(DomainGrammar.ASSOCIATION_ENTITY)) {
			definition = createAssociationSimpleDefinition(definitionSpace, xdefinition);
		} else if (entity.equals(DomainGrammar.ASSOCIATION_NN_ENTITY)) {
			definition = createAssociationNNDefinition(definitionSpace, xdefinition);
		} else if (entity.equals(DomainGrammar.CONSTRAINT_ENTITY)) {
			definition = createConstraint(xdefinition);
		} else if (entity.equals(DomainGrammar.FORMATTER_ENTITY)) {
			definition = createFormatter(xdefinition);
		} else {
			throw new IllegalArgumentException("Type de définition non gérée: " + xdefinition.getName());
		}
		return Option.some(definition);
	}

	/**
	 * Enregistrement de contrainte
	 *
	 * @param xconstraint Définition de contrainte
	 * @return DefinitionStandard Définition typée créée.
	 */
	private static ConstraintDefinition createConstraint(final DynamicDefinition xconstraint) {
		//On transforme la liste des paramètres (Liste de String) sous forme de tableau de String pour éviter
		//le sous typage de List et pour se rapprocher de la syntaxe connue de Main.
		final String name = xconstraint.getName();
		final String args = getPropertyValueAsString(xconstraint, KspProperty.ARGS);
		final String msg = getPropertyValueAsString(xconstraint, KspProperty.MSG);
		final String className = getPropertyValueAsString(xconstraint, KspProperty.CLASS_NAME);
		return new ConstraintDefinition(name, className, msg, args);
	}

	private static FormatterDefinition createFormatter(final DynamicDefinition xformatter) {
		final String name = xformatter.getName();
		final String args = getPropertyValueAsString(xformatter, KspProperty.ARGS);
		final String className = getPropertyValueAsString(xformatter, KspProperty.CLASS_NAME);
		return new FormatterDefinition(name, className, args);
	}

	private Domain createDomain(final DefinitionSpace definitionSpace, final DynamicDefinition xdomain) {
		//il y a deux cas
		//avec formatter et constraint
		final DataType dataType = DataType.valueOf(xdomain.getDefinitionName("dataType"));
		final String domainName = xdomain.getName();
		final boolean hasFormatter = xdomain.containsDefinitionName("formatter");
		final List<String> constraintNames = xdomain.getDefinitionNames("constraint");

		final FormatterDefinition formatter;
		if (hasFormatter) {
			final String formatterName = xdomain.getDefinitionName("formatter");
			formatter = definitionSpace.resolve(formatterName, FormatterDefinition.class);
		} else {
			formatter = null;
		}
		return new Domain(domainName, dataType, formatter, createConstraints(definitionSpace, constraintNames), extractProperties(xdomain));
	}

	/**
	 * @param xdtDefinition Définition de DT
	 */
	private DtDefinition createDtDefinition(final DefinitionSpace definitionSpace, final DynamicDefinition xdtDefinition) {
		//Déclaration de la définition
		final String sortFieldName = (String) xdtDefinition.getPropertyValue(KspProperty.SORT_FIELD);
		final String displayFieldName = (String) xdtDefinition.getPropertyValue(KspProperty.DISPLAY_FIELD);
		//-----
		final String tmpStereotype = (String) xdtDefinition.getPropertyValue(KspProperty.STEREOTYPE);
		//Si Stereotype est non renseigné on suppose que la définition est DtStereotype.Data.
		final DtStereotype stereotype = tmpStereotype != null ? DtStereotype.valueOf(tmpStereotype) : DtStereotype.Data;
		//-----
		final String storeName = (String) xdtDefinition.getPropertyValue(KspProperty.STORE_NAME);
		//-----
		final Boolean persistent = (Boolean) xdtDefinition.getPropertyValue(KspProperty.PERSISTENT);
		Assertion.checkNotNull(persistent, "Le mot-clé ''persistent'' est obligatoire sur une DtDefinition ({0}).", xdtDefinition.getName());
		//-----
		final Boolean tmpDynamic = (Boolean) xdtDefinition.getPropertyValue(KspProperty.DYNAMIC);
		//Si DYNAMIC est non renseigné on suppose que le champ est non dynamic.
		final boolean dynamic = tmpDynamic != null && tmpDynamic.booleanValue();
		//-----
		final String dtDefinitionName = xdtDefinition.getName();
		final DtDefinitionBuilder dtDefinitionBuilder = new DtDefinitionBuilder(dtDefinitionName)
				.withPackageName(xdtDefinition.getPackageName())
				.withStereoType(stereotype)
				.withPersistent(persistent)
				.withDynamic(dynamic)
				.withStoreName(storeName);
		//On enregistre les Builder pour pouvoir les mettre à jour sur les associations.
		Assertion.checkArgument(!dtDefinitionBuilders.containsKey(dtDefinitionName), "Definition '{0}' déjà enregistrée", dtDefinitionName);
		dtDefinitionBuilders.put(dtDefinitionName, dtDefinitionBuilder);

		//Déclaration de la clé primaire
		final List<DynamicDefinition> keys = xdtDefinition.getChildDefinitions(DomainGrammar.PRIMARY_KEY);
		populateIdDtField(definitionSpace, dtDefinitionBuilder, keys, sortFieldName, displayFieldName);

		//Déclaration des champs du DT
		final List<DynamicDefinition> fields = xdtDefinition.getChildDefinitions(DomainGrammar.FIELD);
		populateDataDtField(definitionSpace, dtDefinitionBuilder, fields, sortFieldName, displayFieldName);

		//Déclaration des champs calculés
		final List<DynamicDefinition> computedFields = xdtDefinition.getChildDefinitions(DomainGrammar.COMPUTED);
		populateComputedDtField(definitionSpace, dtDefinitionBuilder, computedFields, sortFieldName, displayFieldName);

		final DtDefinition dtDefinition = dtDefinitionBuilder.build();

		//--Vérification du champ sort et display--
		final boolean sortEmpty = sortFieldName == null && dtDefinition.getSortField().isEmpty();
		final boolean sortNotEmpty = sortFieldName != null && dtDefinition.getSortField().isDefined();

		Assertion.checkState(sortEmpty || sortNotEmpty, "Champ de tri {0} inconnu", sortFieldName);

		final boolean displayEmpty = displayFieldName == null && dtDefinition.getDisplayField().isEmpty();
		final boolean displayNotEmpty = displayFieldName != null && dtDefinition.getDisplayField().isDefined();

		Assertion.checkState(displayEmpty || displayNotEmpty, "Champ d'affichage {0} inconnu", displayFieldName);
		//--Vérification OK
		return dtDefinition;
	}

	/**
	 * Ajoute une liste de champs d'un certain type à la dtDefinition
	 *
	 * @param fields List
	 */
	private void populateIdDtField(final DefinitionSpace definitionSpace, final DtDefinitionBuilder dtDefinitionBuilder, final List<DynamicDefinition> fields, final String sortFieldName, final String displayFieldName) {
		for (final DynamicDefinition field : fields) {
			final Domain domain = definitionSpace.resolve(field.getDefinitionName("domain"), Domain.class);
			//--
			Assertion.checkArgument(field.getPropertyNames().contains(KspProperty.LABEL), "Label est une propriété obligatoire");
			final String label = (String) field.getPropertyValue(KspProperty.LABEL);
			//--
			final String fieldName = field.getName();
			final boolean sort = fieldName.equals(sortFieldName);
			final boolean display = fieldName.equals(displayFieldName);
			//-----
			dtDefinitionBuilder.addIdField(fieldName, label, domain, sort, display);
		}
	}

	/**
	 * Ajoute une liste de champs d'un certain type à la dtDefinition
	 *
	 * @param fields List
	 */
	private void populateDataDtField(final DefinitionSpace definitionSpace, final DtDefinitionBuilder dtDefinitionBuilder, final List<DynamicDefinition> fields, final String sortFieldName, final String displayFieldName) {
		for (final DynamicDefinition field : fields) {
			final Domain domain = definitionSpace.resolve(field.getDefinitionName("domain"), Domain.class);
			//--
			Assertion.checkArgument(field.getPropertyNames().contains(KspProperty.LABEL), "Label est une propriété obligatoire");
			final String label = (String) field.getPropertyValue(KspProperty.LABEL);
			//--
			final boolean notNull = ((Boolean) field.getPropertyValue(KspProperty.NOT_NULL)).booleanValue();
			Assertion.checkArgument(field.getPropertyNames().contains(KspProperty.NOT_NULL), "Not null est une propriété obligatoire.");
			//--
			final Boolean tmpPersistent = (Boolean) field.getPropertyValue(KspProperty.PERSISTENT);
			//Si PERSISTENT est non renseigné on suppose que le champ est à priori persistant .
			final boolean persistent = tmpPersistent == null || tmpPersistent.booleanValue();
			//--
			final String fieldName = field.getName();
			final boolean sort = fieldName.equals(sortFieldName);
			final boolean display = fieldName.equals(displayFieldName);
			//-----
			dtDefinitionBuilder.addDataField(fieldName, label, domain, notNull, persistent, sort, display);
		}
	}

	/**
	 * Ajoute une liste de champs d'un certain type à la dtDefinition
	 *
	 * @param fields List
	 */
	private void populateComputedDtField(final DefinitionSpace definitionSpace, final DtDefinitionBuilder dtDefinitionBuilder, final List<DynamicDefinition> fields, final String sortFieldName, final String displayFieldName) {
		for (final DynamicDefinition field : fields) {
			final Domain domain = definitionSpace.resolve(field.getDefinitionName("domain"), Domain.class);
			//--
			Assertion.checkArgument(field.getPropertyNames().contains(KspProperty.LABEL), "Label est une propriété obligatoire");
			final String label = (String) field.getPropertyValue(KspProperty.LABEL);
			//---
			final String expression = (String) field.getPropertyValue(KspProperty.EXPRESSION);
			final ComputedExpression computedExpression = new ComputedExpression(expression);
			//--
			final String fieldName = field.getName();
			final boolean sort = fieldName.equals(sortFieldName);
			final boolean display = fieldName.equals(displayFieldName);

			dtDefinitionBuilder.addComputedField(fieldName, label, domain, computedExpression, sort, display);
		}
	}

	private AssociationNNDefinition createAssociationNNDefinition(final DefinitionSpace definitionSpace, final DynamicDefinition xassociation) {
		final String tableName = getPropertyValueAsString(xassociation, KspProperty.TABLE_NAME);

		final DtDefinition dtDefinitionA = definitionSpace.resolve(xassociation.getDefinitionName("dtDefinitionA"), DtDefinition.class);
		final boolean navigabilityA = getPropertyValueAsBoolean(xassociation, KspProperty.NAVIGABILITY_A);
		final String roleA = getPropertyValueAsString(xassociation, KspProperty.ROLE_A);
		final String labelA = getPropertyValueAsString(xassociation, KspProperty.LABEL_A);

		final DtDefinition dtDefinitionB = definitionSpace.resolve(xassociation.getDefinitionName("dtDefinitionB"), DtDefinition.class);
		final boolean navigabilityB = getPropertyValueAsBoolean(xassociation, KspProperty.NAVIGABILITY_B);
		final String roleB = getPropertyValueAsString(xassociation, KspProperty.ROLE_B);
		final String labelB = getPropertyValueAsString(xassociation, KspProperty.LABEL_B);

		final AssociationNode associationNodeA = new AssociationNode(dtDefinitionA, navigabilityA, roleA, labelA, true, false);
		final AssociationNode associationNodeB = new AssociationNode(dtDefinitionB, navigabilityB, roleB, labelB, true, false);
		final String name = fixAssociationName(ASSOCIATION_NN_DEFINITION_PREFIX, xassociation.getName());
		return new AssociationNNDefinition(name, tableName, associationNodeA, associationNodeB);
	}

	// méthode permettant de créer une liste de contraintes à partir d'une liste de noms de contrainte
	private List<ConstraintDefinition> createConstraints(final DefinitionSpace definitionSpace, final List<String> constraintNames) {
		final List<ConstraintDefinition> constraints = new ArrayList<>(constraintNames.size());
		for (final String constraintName : constraintNames) {
			constraints.add(definitionSpace.resolve(constraintName, ConstraintDefinition.class));
		}
		return constraints;
	}

	/**
	 * Corrige le nom des associations qui ne respectent pas la règle de nommage.
	 * @param name Nom de l'association
	 * @return Nom corrigé de l'association comprenant le préfix obligatoire.
	 */
	private static String fixAssociationName(final String prefix, final String name) {
		if (!name.startsWith(prefix + SEPARATOR)) {
			return prefix + SEPARATOR + name;
		}
		return name;
	}

	private AssociationSimpleDefinition createAssociationSimpleDefinition(final DefinitionSpace definitionSpace, final DynamicDefinition xassociation) {
		final String fkFieldName = getPropertyValueAsString(xassociation, KspProperty.FK_FIELD_NAME);

		final DtDefinition dtDefinitionA = definitionSpace.resolve(xassociation.getDefinitionName("dtDefinitionA"), DtDefinition.class);
		final String multiplicityA = getPropertyValueAsString(xassociation, KspProperty.MULTIPLICITY_A);
		final boolean navigabilityA = getPropertyValueAsBoolean(xassociation, KspProperty.NAVIGABILITY_A).booleanValue();
		final String roleA = getPropertyValueAsString(xassociation, KspProperty.ROLE_A);
		final String labelA = getPropertyValueAsString(xassociation, KspProperty.LABEL_A);

		final DtDefinition dtDefinitionB = definitionSpace.resolve(xassociation.getDefinitionName("dtDefinitionB"), DtDefinition.class);
		final String multiplicityB = getPropertyValueAsString(xassociation, KspProperty.MULTIPLICITY_B);
		final boolean navigabilityB = getPropertyValueAsBoolean(xassociation, KspProperty.NAVIGABILITY_B).booleanValue();
		final String roleB = getPropertyValueAsString(xassociation, KspProperty.ROLE_B);
		final String labelB = getPropertyValueAsString(xassociation, KspProperty.LABEL_B);

		// Vérification que l'on est bien dans le cas d'une association simple de type 1-n
		if (AssociationUtil.isMultiple(multiplicityB) && AssociationUtil.isMultiple(multiplicityA)) {
			//Relation n-n
			throw new IllegalArgumentException("Utiliser la déclaration AssociationNN");
		}
		if (!AssociationUtil.isMultiple(multiplicityB) && !AssociationUtil.isMultiple(multiplicityA)) {
			//Relation 1-1
			throw new IllegalArgumentException("Les associations 1-1 sont interdites");
		}

		//Relation 1-n ou 1-1
		final String urn = fixAssociationName(ASSOCIATION_SIMPLE_DEFINITION_PREFIX, xassociation.getName());
		final AssociationSimpleDefinition associationSimpleDefinition = AssociationSimpleDefinition.createAssociationSimpleDefinition(urn, fkFieldName,
				dtDefinitionA, navigabilityA, roleA, labelA, AssociationUtil.isMultiple(multiplicityA), AssociationUtil.isNotNull(multiplicityA),
				dtDefinitionB, navigabilityB, roleB, labelB, AssociationUtil.isMultiple(multiplicityB), AssociationUtil.isNotNull(multiplicityB));

		final AssociationNode primaryAssociationNode = associationSimpleDefinition.getPrimaryAssociationNode();
		final AssociationNode foreignAssociationNode = associationSimpleDefinition.getForeignAssociationNode();

		final DtDefinition fkDefinition = primaryAssociationNode.getDtDefinition();

		LOGGER.trace("" + xassociation.getName() + " : ajout d'une FK [" + fkFieldName + "] sur la table '" + foreignAssociationNode.getDtDefinition().getName() + "'");

		final String label = primaryAssociationNode.getLabel();
		dtDefinitionBuilders.get(foreignAssociationNode.getDtDefinition().getName()).addForeignKey(fkFieldName, label, fkDefinition.getIdField().get().getDomain(), primaryAssociationNode.isNotNull(), fkDefinition.getName(), false, false); //On estime qu'une FK n'est ni une colonne de tri ni un champ d'affichage

		return associationSimpleDefinition;
	}

	/**
	 * Extrait le PropertyContainer<DtProperty> d'une DynamicDefinition.
	 * Associe les DtProperty et les KspProperty par leur nom.
	 * @param dynamicDefinition Definition
	 * @return Container des propriétés
	 */
	private static Properties extractProperties(final DynamicDefinition dynamicDefinition) {
		final PropertiesBuilder propertiesBuilder = new PropertiesBuilder();

		//On associe les propriétés Dt et Ksp par leur nom.
		for (final String entityPropertyName : dynamicDefinition.getPropertyNames()) {
			final Property property = DtProperty.valueOf(entityPropertyName);
			propertiesBuilder.addValue(property, dynamicDefinition.getPropertyValue(entityPropertyName));
		}
		return propertiesBuilder.build();
	}

	/** {@inheritDoc} */
	@Override
	public void onNewDefinition(final DynamicDefinition xdefinition, final DynamicDefinitionRepository dynamicModelrepository) {
		if (xdefinition.getEntity().equals(DomainGrammar.DT_DEFINITION_ENTITY)) {
			//Dans le cas des DT on ajoute les domaines
			registerxxxxDomain(xdefinition.getName(), xdefinition.getPackageName(), dynamicModelrepository);
		}
	}

	/*
	 * Construction des deux domaines relatif à un DT : DO_DT_XXX_DTO et DO_DT_XXX_DTC
	 */
	private static void registerxxxxDomain(final String definitionName, final String packageName, final DynamicDefinitionRepository dynamicModelRepository) {
		//C'est le constructeur de DtDomainStandard qui vérifie la cohérence des données passées.
		//Notamment la validité de la liste des contraintes et la nullité du formatter

		final Entity metaDefinitionDomain = DomainGrammar.DOMAIN_ENTITY;

		final DynamicDefinition domain = DynamicDefinitionRepository.createDynamicDefinitionBuilder(DOMAIN_PREFIX + SEPARATOR + definitionName + "_DTO", metaDefinitionDomain, packageName)
				.addDefinition("dataType", "DtObject")
				//On dit que le domaine possède une prop définissant le type comme étant le nom du DT
				.addPropertyValue(KspProperty.TYPE, definitionName)
				.build();

		//On ajoute le domain crée au repository
		dynamicModelRepository.addDefinition(domain);

		//On fait la même chose avec DTC

		final DynamicDefinition domain2 = DynamicDefinitionRepository.createDynamicDefinitionBuilder(DOMAIN_PREFIX + SEPARATOR + definitionName + "_DTC", metaDefinitionDomain, packageName)
				.addDefinition("dataType", "DtList")
				//On dit que le domaine possède une prop définissant le type comme étant le nom du DT
				.addPropertyValue(KspProperty.TYPE, definitionName)
				.build();

		//On ajoute le domain crée au repository
		dynamicModelRepository.addDefinition(domain2);
	}
}
