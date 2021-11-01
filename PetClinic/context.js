function getOwners(request, response, db) {
	const last_name = request.parameters.lastName[0];
	let filteredOwners = {};
	for (let owner_id in db.owners.data) {
		let owner = db.owners.data[owner_id];
		let okOwner = last_name.length == 0 ? true : owner.last_name.startsWith(last_name);
		if (okOwner) {
			owner['petNames'] = '';
			let first = true;
			for (let pet_id in db.pets.data) {
				let pet = db.pets.data[pet_id];
				let okPet = pet.owner_id == owner_id;
				if (okPet) {
					if (first)
						first = false;
					else
						owner['petNames'] += ', ';
					owner['petNames'] += pet.name;
				}
			}
			filteredOwners[owner_id] = owner;
		}
	}
	response.body = filteredOwners;
}

function getOwner(request, response, db) {
	const id = request.parameters.id[0];
	const owner = db.owners.data[id];
	if (owner == undefined)
		response.status = 404;
	else
		response.body = owner;
}

function getPets(request, response, db) {
	const owner_id = request.parameters.id[0];
	const owner = db.owners.data[owner_id];
	if (owner == undefined)
		response.status = 404;
	else {
		const pets = {};
		for (let pet_id in db.pets.data) {
			let pet = db.pets.data[pet_id];
			let okPet = pet.owner_id == owner_id;
			if (okPet) {
				let type_id = pet.type_id;
				let type = db.types.data[type_id];
				pet['type_name'] = type.name;
				pets[pet_id] = pet;
			}
		}
		response.body = pets;
	}
}

function getPet(request, response, db) {
	const pet_id = request.parameters.id[0];
	const pet = db.pets.data[pet_id];
	if (pet == undefined)
		response.status = 404;
	else
		response.body = pet;
}

function newPet(request, response, db) {
	const pet = {
		owner_id: request.parameters.id[0],
		name: request.parameters.name[0],
		birth_date: request.parameters.birth_date[0],
		type_id: request.parameters.type_id[0]
	};

	let id = insert(db.pets, pet);

	response.body = { id: id };
}

function updatePet(request, response, db) {
	const pet_id = request.parameters.id[0];
	const pet = db.pets.data[pet_id];
	if (pet == undefined)
		response.status = 404;
	else {
		pet['owner_id'] = request.parameters.ownerId[0];
		pet['name'] = request.parameters.name[0];
		pet['birth_date'] = request.parameters.birthDate[0];
		pet['type_id'] = request.parameters.typeId[0];
	}
}

function getTypes(request, response, db) {
	response.body = db.types.data;
}

function getVisits(request, response, db) {
	const pet_id = request.parameters.id[0];
	const pet = db.pets.data[pet_id];
	if (pet == undefined)
		response.status = 404;
	else {
		const visits = {};
		for (let visit_id in db.visits.data) {
			let visit = db.visits.data[visit_id];
			let okVisit = visit.pet_id == pet_id;
			if (okVisit)
				visits[visit_id] = visit;
		}
		response.body = visits;
	}
}

function newVisit(request, response, db) {
	const visit = {
		pet_id: request.parameters.id[0],
		visit_date: request.parameters.visitDate[0],
		description: request.parameters.description[0]
	};

	let id = insert(db.visits, visit);

	response.body = { id: id };
}


function newOwner(request, response, db) {
	let owner = {
		first_name: request.parameters.first_name[0],
		last_name: request.parameters.last_name[0],
		address: request.parameters.address[0],
		city: request.parameters.city[0],
		telephone: request.parameters.telephone[0]
	}
	let id = insert(db.owners, owner);

	response.body = { id: id };
}

function updateOwner(request, response, db) {
	const owner_id = request.parameters.id[0];
	const owner = db.owners.data[owner_id];
	if (owner == undefined)
		response.status = 404;
	else {
		owner['first_name'] = request.parameters.first_name[0];
		owner['last_name'] = request.parameters.last_name[0];
		owner['address'] = request.parameters.address[0];
		owner['city'] = request.parameters.city[0];
		owner['telephone'] = request.parameters.telephone[0];
	}
}

function getVets(request, response, db) {
	const vets = {};
	for (let vet_id in db.vets.data) {
		let vet = db.vets.data[vet_id];
		let specialty_names = '';
		let first = true;
		for (let i = 0; i < db.vet_specialties.length; i++) {
			let vet_specialty = db.vet_specialties[i];
			if (vet_specialty.vet_id == vet_id) {
				let specialty_id = vet_specialty.specialty_id
				let specialty_name = db.specialties.data[specialty_id].name;
				if (first)
					first = false;
				else
					specialty_names += ', ';
				specialty_names += specialty_name;
			}
		}
		vet.specialty_names = specialty_names;

		vets[vet_id] = vet;
	}
	response.body = vets;
}

function insert(table, record) {
	let id = table[""];
	id = "" + (parseInt(id) + 1);
	table[""] = id;
	table.data[id] = record;
	return id;
}
