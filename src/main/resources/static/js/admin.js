document.addEventListener('DOMContentLoaded', () => {
    loadUsers();
    loadNewUserRoles();

    const editUserForm = document.getElementById('editUserForm');
    const deleteUserForm = document.getElementById('deleteUserForm');
    const newUserForm = document.getElementById('newUserForm');

    if (editUserForm) {
        editUserForm.addEventListener('submit', updateUser);
    }

    if (deleteUserForm) {
        deleteUserForm.addEventListener('submit', deleteUser);
    }

    if (newUserForm) {
        newUserForm.addEventListener('submit', createUser);
    }
});

async function loadUsers() {
    try {
        const response = await fetch('/api/admin/users');

        if (!response.ok) {
            throw new Error('Failed to load users');
        }

        const users = await response.json();

        renderUsers(users);
    } catch (error) {
        console.error(error);
    }
}

function renderUsers(users) {
    const tableBody = document.getElementById('usersTableBody');

    if (!tableBody) {
        return;
    }

    tableBody.innerHTML = '';

    users.forEach(user => {
        const row = document.createElement('tr');

        const roles = user.roles
            .map(role => role.name.replace('ROLE_', ''))
            .join(', ');

        row.innerHTML = `
            <td>${escapeHtml(user.id)}</td>
            <td>${escapeHtml(user.firstName)}</td>
            <td>${escapeHtml(user.lastName)}</td>
            <td>${escapeHtml(user.age)}</td>
            <td>${escapeHtml(user.email)}</td>
            <td>${escapeHtml(roles)}</td>
            <td>
                <button
                    type="button"
                    class="btn btn-info btn-sm text-white"
                    onclick="openEditModal(${user.id})">
                    Edit
                </button>
            </td>
            <td>
                <button
                    type="button"
                    class="btn btn-danger btn-sm"
                    onclick="openDeleteModal(${user.id})">
                    Delete
                </button>
            </td>
        `;

        tableBody.appendChild(row);
    });
}

async function loadNewUserRoles() {
    const container = document.getElementById('newUserRoles');

    if (!container) {
        return;
    }

    try {
        const response = await fetch('/api/admin/roles');

        if (!response.ok) {
            throw new Error('Failed to load roles');
        }

        const roles = await response.json();

        renderNewUserRoles(roles);
    } catch (error) {
        console.error(error);

        showError(
            'newUserError',
            'Unable to load roles'
        );
    }
}

function renderNewUserRoles(roles) {
    const container = document.getElementById('newUserRoles');

    if (!container) {
        return;
    }

    container.innerHTML = '';

    roles.forEach(role => {
        const wrapper = document.createElement('div');

        wrapper.className = 'form-check mb-2';

        wrapper.innerHTML = `
            <input
                class="form-check-input new-user-role"
                type="checkbox"
                id="new-role-${role.id}"
                value="${role.id}">

            <label
                class="form-check-label"
                for="new-role-${role.id}">
                ${escapeHtml(
            role.name.replace('ROLE_', '')
        )}
            </label>
        `;

        container.appendChild(wrapper);
    });
}

async function createUser(event) {
    event.preventDefault();

    hideError('newUserError');

    const form = event.currentTarget;

    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    const roleIds = Array.from(
        document.querySelectorAll(
            '.new-user-role:checked'
        )
    ).map(role => Number(role.value));

    const user = {
        firstName:
            document.getElementById(
                'newFirstName'
            ).value.trim(),

        lastName:
            document.getElementById(
                'newLastName'
            ).value.trim(),

        age:
            Number(
                document.getElementById(
                    'newAge'
                ).value
            ),

        email:
            document.getElementById(
                'newEmail'
            ).value.trim(),

        password:
        document.getElementById(
            'newPassword'
        ).value,

        roleIds: roleIds
    };

    try {
        const response = await fetch(
            '/api/admin/users',
            {
                method: 'POST',
                headers: getJsonHeaders(),
                body: JSON.stringify(user)
            }
        );

        if (!response.ok) {
            const error =
                await readErrorResponse(response);

            showError(
                'newUserError',
                error
            );

            return;
        }

        form.reset();

        await loadUsers();

        const usersTabElement =
            document.getElementById('users-tab');

        const usersTab =
            bootstrap.Tab.getOrCreateInstance(
                usersTabElement
            );

        usersTab.show();
    } catch (error) {
        console.error(error);

        showError(
            'newUserError',
            'Unable to create user'
        );
    }
}

async function openEditModal(id) {
    hideError('editError');

    try {
        const [userResponse, rolesResponse] =
            await Promise.all([
                fetch(`/api/admin/users/${id}`),
                fetch('/api/admin/roles')
            ]);

        if (!userResponse.ok) {
            throw new Error('Failed to load user');
        }

        if (!rolesResponse.ok) {
            throw new Error('Failed to load roles');
        }

        const user = await userResponse.json();
        const roles = await rolesResponse.json();

        document.getElementById(
            'editUserId'
        ).value = user.id;

        document.getElementById(
            'editIdDisplay'
        ).value = user.id;

        document.getElementById(
            'editFirstName'
        ).value = user.firstName;

        document.getElementById(
            'editLastName'
        ).value = user.lastName;

        document.getElementById(
            'editAge'
        ).value = user.age;

        document.getElementById(
            'editEmail'
        ).value = user.email;

        document.getElementById(
            'editPassword'
        ).value = '';

        renderEditRoles(
            roles,
            user.roles
        );

        const modalElement =
            document.getElementById(
                'editUserModal'
            );

        const modal =
            bootstrap.Modal.getOrCreateInstance(
                modalElement
            );

        modal.show();
    } catch (error) {
        console.error(error);
    }
}

function renderEditRoles(roles, selectedRoles) {
    const container =
        document.getElementById('editRoles');

    container.innerHTML = '';

    roles.forEach(role => {
        const wrapper =
            document.createElement('div');

        wrapper.className =
            'form-check mb-2';

        const checked =
            selectedRoles.some(
                selectedRole =>
                    selectedRole.id === role.id
            );

        wrapper.innerHTML = `
            <input
                class="form-check-input edit-role"
                type="checkbox"
                id="edit-role-${role.id}"
                value="${role.id}"
                ${checked ? 'checked' : ''}>

            <label
                class="form-check-label"
                for="edit-role-${role.id}">
                ${escapeHtml(
            role.name.replace(
                'ROLE_',
                ''
            )
        )}
            </label>
        `;

        container.appendChild(wrapper);
    });
}

async function updateUser(event) {
    event.preventDefault();

    hideError('editError');

    const form = event.currentTarget;

    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    const id =
        document.getElementById(
            'editUserId'
        ).value;

    const roleIds =
        Array.from(
            document.querySelectorAll(
                '.edit-role:checked'
            )
        ).map(role =>
            Number(role.value)
        );

    const user = {
        firstName:
            document.getElementById(
                'editFirstName'
            ).value.trim(),

        lastName:
            document.getElementById(
                'editLastName'
            ).value.trim(),

        age:
            Number(
                document.getElementById(
                    'editAge'
                ).value
            ),

        email:
            document.getElementById(
                'editEmail'
            ).value.trim(),

        password:
        document.getElementById(
            'editPassword'
        ).value,

        roleIds: roleIds
    };

    try {
        const response =
            await fetch(
                `/api/admin/users/${id}`,
                {
                    method: 'PUT',
                    headers: getJsonHeaders(),
                    body: JSON.stringify(user)
                }
            );

        if (!response.ok) {
            const error =
                await readErrorResponse(
                    response
                );

            showError(
                'editError',
                error
            );

            return;
        }

        const modalElement =
            document.getElementById(
                'editUserModal'
            );

        const modal =
            bootstrap.Modal.getInstance(
                modalElement
            );

        if (modal) {
            modal.hide();
        }

        await loadUsers();
    } catch (error) {
        console.error(error);

        showError(
            'editError',
            'Unable to update user'
        );
    }
}

async function openDeleteModal(id) {
    hideError('deleteError');

    try {
        const response =
            await fetch(
                `/api/admin/users/${id}`
            );

        if (!response.ok) {
            throw new Error(
                'Failed to load user'
            );
        }

        const user =
            await response.json();

        document.getElementById(
            'deleteUserId'
        ).value = user.id;

        document.getElementById(
            'deleteIdDisplay'
        ).value = user.id;

        document.getElementById(
            'deleteFirstName'
        ).value = user.firstName;

        document.getElementById(
            'deleteLastName'
        ).value = user.lastName;

        document.getElementById(
            'deleteAge'
        ).value = user.age;

        document.getElementById(
            'deleteEmail'
        ).value = user.email;

        document.getElementById(
            'deleteRoles'
        ).value = user.roles
            .map(role =>
                role.name.replace(
                    'ROLE_',
                    ''
                )
            )
            .join(', ');

        const modalElement =
            document.getElementById(
                'deleteUserModal'
            );

        const modal =
            bootstrap.Modal.getOrCreateInstance(
                modalElement
            );

        modal.show();
    } catch (error) {
        console.error(error);
    }
}

async function deleteUser(event) {
    event.preventDefault();

    hideError('deleteError');

    const id =
        document.getElementById(
            'deleteUserId'
        ).value;

    try {
        const response =
            await fetch(
                `/api/admin/users/${id}`,
                {
                    method: 'DELETE',
                    headers: getCsrfHeaders()
                }
            );

        if (!response.ok) {
            const error =
                await readErrorResponse(
                    response
                );

            showError(
                'deleteError',
                error
            );

            return;
        }

        const modalElement =
            document.getElementById(
                'deleteUserModal'
            );

        const modal =
            bootstrap.Modal.getInstance(
                modalElement
            );

        if (modal) {
            modal.hide();
        }

        await loadUsers();
    } catch (error) {
        console.error(error);

        showError(
            'deleteError',
            'Unable to delete user'
        );
    }
}

function getJsonHeaders() {
    const headers = {
        'Content-Type': 'application/json'
    };

    const csrfHeader =
        document.querySelector(
            'meta[name="_csrf_header"]'
        )?.content;

    const csrfToken =
        document.querySelector(
            'meta[name="_csrf"]'
        )?.content;

    if (csrfHeader && csrfToken) {
        headers[csrfHeader] =
            csrfToken;
    }

    return headers;
}

function getCsrfHeaders() {
    const headers = {};

    const csrfHeader =
        document.querySelector(
            'meta[name="_csrf_header"]'
        )?.content;

    const csrfToken =
        document.querySelector(
            'meta[name="_csrf"]'
        )?.content;

    if (csrfHeader && csrfToken) {
        headers[csrfHeader] =
            csrfToken;
    }

    return headers;
}

async function readErrorResponse(response) {
    try {
        const data =
            await response.json();

        if (data.message) {
            return data.message;
        }

        const messages =
            Object.values(data);

        if (messages.length > 0) {
            return messages.join('\n');
        }

        return 'Request failed';
    } catch (error) {
        return 'Request failed';
    }
}

function showError(elementId, message) {
    const element =
        document.getElementById(
            elementId
        );

    if (!element) {
        return;
    }

    element.textContent =
        message;

    element.classList.remove(
        'd-none'
    );
}

function hideError(elementId) {
    const element =
        document.getElementById(
            elementId
        );

    if (!element) {
        return;
    }

    element.textContent = '';

    element.classList.add(
        'd-none'
    );
}

function escapeHtml(value) {
    if (value === null ||
        value === undefined) {

        return '';
    }

    const div =
        document.createElement('div');

    div.textContent =
        String(value);

    return div.innerHTML;
}