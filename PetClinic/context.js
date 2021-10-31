function getOwners(request, response, data) {
	const last_name = request.parameters.lastName[0];
	let filteredOwners = {};
	for (let owner_id in data.owners) {
		let owner = data.owners[owner_id];
		let okOwner = last_name.length == 0 ? true : owner.last_name.startsWith(last_name);
		if (okOwner) {
			owner['petNames'] = '';
			let first = true;
			for (let pet_id in data.pets) {
				let pet = data.pets[pet_id];
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

function getOwner(request, response, data) {
	const id = request.parameters.id[0];
	const owner = data.owners[id];
	if (owner == undefined)
		response.status = 404;
	else
		response.body = owner;
}

function getPets(request, response, data) {
	const owner_id = request.parameters.id[0];
	const owner = data.owners[owner_id];
	if (owner == undefined)
		response.status = 404;
	else {
		const pets = {};
		for (let pet_id in data.pets) {
			let pet = data.pets[pet_id];
			let okPet = pet.owner_id == owner_id;
			if (okPet) {
				let type_id = pet.type_id;
				let type = data.types[type_id];
				pet['type_name'] = type.name;
				pets[pet_id] = pet;
			}
		}
		response.body = pets;
	}
}

function getPet(request, response, data) {
	const pet_id = request.parameters.id[0];
	const pet = data.pets[pet_id];
	if (pet == undefined)
		response.status = 404;
	else
		response.body = pet;
}

function newPet(request, response, data) {
	const pet = {
		owner_id: request.parameters.id[0],
		name: request.parameters.name[0],
		birth_date: request.parameters.birth_date[0],
		type_id: request.parameters.type_id[0]
	};

	data.pets_id = "" + (parseInt(data.pets_id) + 1);
	data.pets[data.pets_id] = pet;

	response.body = { id: data.pets_id };
}

function updatePet(request, response, data) {
	const pet_id = request.parameters.id[0];
	const pet = data.pets[pet_id];
	if (pet == undefined)
		response.status = 404;
	else {
		pet['owner_id'] = request.parameters.ownerId[0];
		pet['name'] = request.parameters.name[0];
		pet['birth_date'] = request.parameters.birthDate[0];
		pet['type_id'] = request.parameters.typeId[0];
	}
}

function getTypes(request, response, data) {
	response.body = data.types;
}

function getVisits(request, response, data) {
	const pet_id = request.parameters.id[0];
	const pet = data.pets[pet_id];
	if (pet == undefined)
		response.status = 404;
	else {
		const visits = {};
		for (let visit_id in data.visits) {
			let visit = data.visits[visit_id];
			let okVisit = visit.pet_id == pet_id;
			if (okVisit)
				visits[visit_id] = visit;
		}
		response.body = visits;
	}
}

function newVisit(request, response, data) {
	const visit = {
		pet_id: request.parameters.id[0],
		visit_date: request.parameters.visitDate[0],
		description: request.parameters.description[0]
	};

	data.visits_id = "" + (parseInt(data.visits_id) + 1);
	data.visits[data.visits_id] = visit;

	response.body = { id: data.visits_id };
}


function newOwner(request, response, data) {
	let owner = {};

	owner['first_name'] = request.parameters.first_name[0];
	owner['last_name'] = request.parameters.last_name[0];
	owner['address'] = request.parameters.address[0];
	owner['city'] = request.parameters.city[0];
	owner['telephone'] = request.parameters.telephone[0];

	data.owners_id = "" + (parseInt(data.owners_id) + 1);
	data.owners[data.owners_id] = owner;

	response.body = { id: data.owners_id };
}

function updateOwner(request, response, data) {
	const owner_id = request.parameters.id[0];
	const owner = data.owners[owner_id];
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

function getVets(request, response, data) {
	const vets = {};
	for (let vet_id in data.vets) {
		let vet = data.vets[vet_id];
	    let specialty_names = '';
	    let first = true;
	    for (let i = 0; i < data.vet_specialties.length; i++) {
	    	let vet_specialty = data.vet_specialties[i];
	    	if (vet_specialty.vet_id == vet_id) {
	    		let specialty_id = vet_specialty.specialty_id
	    		let specialty_name = data.specialties[specialty_id].name;
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
