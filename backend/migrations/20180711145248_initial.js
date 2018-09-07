/**
 * SQL table definitions with Knex.
 */

/** */
exports.up = function (knex, Promise) {
    return knex
        .schema

        // Users
        .createTable('users', table => {
            table.increments('id').primary();
            table.string('email');
            table.string('password', 257);
            table.string('account');
            table.uuid('apiminerId');
            table.string('apiminerToken');
            table.dateTime('created_at').notNullable().defaultTo(knex.raw('CURRENT_TIMESTAMP'));

            table.unique('email');
        })

        // Access Tokens
        .createTable('access', table => {
            table.increments('id').primary();
            table.integer('user', 11).unsigned().references('id').inTable('users');
            table.string('token');
            table.integer('expiration');
            table.dateTime('created_at').notNullable().defaultTo(knex.raw('CURRENT_TIMESTAMP'));

            table.unique('token');
        })
};

exports.down = function (knex, Promise) {
    return knex.schema
        .dropTable('users')
        .dropTable('access');
};
