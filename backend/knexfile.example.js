module.exports = {
    development: {
        client: 'sqlite3',
        connection: {
            filename: './dev.sqlite3'
        },
        useNullAsDefault: true
    },
    production: {
        client: 'mysql2',
        version: '5.7',
        connection: {
            database: 'apiminer_wallet',
            user: '',
            password: '',
            socketPath: ''
        },
        pool: {
            min: 1,
            max: 2
        }
    }
};
